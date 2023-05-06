package xyz.bluspring.kilt.loader.remap

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.fabricmc.loader.impl.util.ManifestUtil
import net.fabricmc.mapping.tree.TinyMappingFactory
import net.fabricmc.mapping.tree.TinyTree
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.tree.ClassNode
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.loader.ForgeMod
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.kilt.loader.fixers.EventClassVisibilityFixer
import xyz.bluspring.kilt.loader.staticfix.StaticAccessFixer
import xyz.bluspring.kilt.loader.superfix.CommonSuperClassWriter
import xyz.bluspring.kilt.loader.superfix.CommonSuperFixer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Function
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

object KiltRemapper {
    private val logger = LoggerFactory.getLogger("Kilt Remapper")
    // This is created automatically using https://github.com/BluSpring/srg2intermediary
    // srg -> intermediary
    val srgIntermediaryTree: TinyTree = TinyMappingFactory.load(this::class.java.getResourceAsStream("/srg_intermediary.tiny")!!.bufferedReader())
    private val kiltWorkaroundTree = TinyMappingFactory.load(this::class.java.getResourceAsStream("/kilt_workaround_mappings.tiny")!!.bufferedReader())

    // Mainly for debugging, so already-remapped Forge mods will be remapped again.
    private val forceRemap = System.getProperty("kilt.forceRemap")?.lowercase() == "true"

    // Mainly for debugging, used to test unobfuscated mods and ensure that Kilt is running as intended.
    private val disableRemaps = System.getProperty("kilt.noRemap")?.lowercase() == "true"

    // SRG class -> Intermediary/Named class
    val classMappings = mutableMapOf<String, String>()

    // SRG field -> Intermediary/Named name + descriptor
    val fieldMappings = mutableMapOf<String, Pair<String, String>>()

    // SRG method name -> descriptor -> Intermediary/Named name + descriptor
    val methodMappings = mutableMapOf<String, Pair<String, String>>()

    private val launcher = FabricLauncherBase.getLauncher()
    internal val useNamed = launcher.targetNamespace != "intermediary"

    private val namespace: String = if (useNamed) launcher.targetNamespace else "intermediary"

    private val mappingCacheFile = File(KiltLoader.kiltCacheDir, "mapping_${KiltLoader.SUPPORTED_FORGE_SPEC_VERSION}_$namespace.txt")
    private val remapper: KiltAsmRemapper

    init {
        val mappings = FabricLauncherBase.getLauncher().mappingConfiguration.mappings

        val start = System.currentTimeMillis()
        logger.info("Loading mappings from Searge to $namespace...")

        if (mappingCacheFile.exists()) {
            logger.info("Found cached mapping file")

            val lines = mappingCacheFile.readLines()
            // Classes
            lines[0].split(",").forEach {
                val split = it.split(">")
                classMappings[split[0]] = split[1]
            }

            // Fields
            lines[1].split(",").forEach {
                val split = it.split(">")
                val descSplit = split[1].split("&")
                fieldMappings[split[0]] = Pair(descSplit[0], descSplit[1])
            }

            // Methods
            lines[2].split(",").forEach {
                val split = it.split(">")
                val descSplit = split[1].split("&")
                methodMappings[split[0]] = Pair(descSplit[0], descSplit[1])
            }
        } else {
            logger.info("No cached mapping file found, this may take a while to load!")
            srgIntermediaryTree.classes.forEach { srgClass ->
                val intermediaryClass =
                    mappings.classes.firstOrNull { it.getName("intermediary") == srgClass.getName("intermediary") }
                        ?: return@forEach

                classMappings[srgClass.getName("searge")] = intermediaryClass.getName(namespace)

                srgClass.fields.forEach field@{ srgField ->
                    val srgName = srgField.getName("searge")

                    if (fieldMappings.contains(srgName))
                        return@field

                    val intermediaryField =
                        intermediaryClass.fields.firstOrNull { it.getName("intermediary") == srgField.getName("intermediary") }

                    if (namespace == "intermediary" || intermediaryField == null) {
                        fieldMappings[srgName] =
                            Pair(srgField.getName("intermediary"), srgField.getDescriptor("intermediary"))

                        if (srgField.getDescriptor("searge").startsWith("("))
                            methodMappings[srgName] = fieldMappings[srgName]!!

                        return@field
                    }

                    fieldMappings[srgName] = Pair(
                        intermediaryField.getName(namespace), intermediaryField.getDescriptor(
                            namespace
                        )
                    )

                    if (srgField.getDescriptor("searge").startsWith("("))
                        methodMappings[srgName] = fieldMappings[srgName]!!
                }

                srgClass.methods.forEach method@{ srgMethod ->
                    val name = srgMethod.getName("searge")

                    // let's not map something that already exists.
                    if (methodMappings.contains(name))
                        return@method

                    // need to use a different way of getting the method, because SRG stores method members in literally everyone
                    val intermediaryClass2 = mappings.classes.firstOrNull {
                        it.methods.any { m ->
                            m.getName("intermediary") == srgMethod.getName("intermediary") && m.getDescriptor("intermediary") == srgMethod.getDescriptor(
                                "intermediary"
                            )
                        }
                    }

                    if (namespace == "intermediary" || intermediaryClass2 == null) {
                        methodMappings[name] =
                            Pair(srgMethod.getName("intermediary"), srgMethod.getDescriptor("intermediary"))
                        return@method
                    }

                    val intermediaryMethod = intermediaryClass2.methods.first {
                        it.getName("intermediary") == srgMethod.getName("intermediary") && it.getDescriptor("intermediary") == srgMethod.getDescriptor(
                            "intermediary"
                        )
                    }

                    methodMappings[name] = Pair(
                        intermediaryMethod.getName(
                            namespace
                        ), intermediaryMethod.getDescriptor(namespace)
                    )
                }
            }

            mappingCacheFile.createNewFile()
            val writer = mappingCacheFile.writer()
            writer.write(classMappings.map { "${it.key}>${it.value}" }.joinToString(","))
            writer.write("\n")
            writer.write(fieldMappings.map { "${it.key}>${it.value.first}&${it.value.second}" }.joinToString(","))
            writer.write("\n")
            writer.write(methodMappings.map { "${it.key}>${it.value.first}&${it.value.second}" }.joinToString(","))
            writer.close()
        }

        logger.info("Finished loading mappings! (took ${System.currentTimeMillis() - start}ms)")

        remapper = KiltAsmRemapper(fieldMappings, methodMappings)
    }

    private lateinit var remappedModsDir: File

    fun remapMods(modLoadingQueue: ConcurrentLinkedQueue<ForgeMod>, remappedModsDir: File): List<Exception> {
        if (disableRemaps) {
            logger.warn("Mod remapping has been disabled! Mods built normally using ForgeGradle will not function with this enabled.")
            logger.warn("Only have this enabled if you know what you're doing!")

            modLoadingQueue.forEach {
                if (it.modFile != null)
                    it.remappedModFile = it.modFile
            }

            return listOf()
        }

        this.remappedModsDir = remappedModsDir

        if (forceRemap)
            logger.warn("Forced remaps enabled! All Forge mods will be remapped.")

        val exceptions = mutableListOf<Exception>()

        val modRemapQueue = ArrayList<ForgeMod>(modLoadingQueue.size).apply {
            addAll(modLoadingQueue)
        }

        // We need to sort it in a way where dependencies are remapped before everyone else,
        // so the mods can remap correctly.
        modRemapQueue.sortWith { a, b ->
            if (a.modInfo.mod.dependencies.any { it.modId == b.modInfo.mod.modId })
                1
            else if (b.modInfo.mod.dependencies.any { it.modId == a.modInfo.mod.modId })
                -1
            else 0
        }

        logger.info("Remapping Forge mods...")

        modRemapQueue.forEach { mod ->
            if (mod.modFile == null)
                return@forEach

            try {
                exceptions.addAll(remapMod(mod.modFile, mod))
                logger.info("Remapped ${mod.modInfo.mod.displayName} (${mod.modInfo.mod.modId})")
            } catch (e: Exception) {
                exceptions.add(e)
                e.printStackTrace()
            }
        }

        logger.info("Finished remapping mods!")

        if (exceptions.isNotEmpty()) {
            logger.error("Ran into some errors, we're not going to continue with the repairing process.")
            return exceptions
        }

        StaticAccessFixer.fixMods(modLoadingQueue, remappedModsDir)

        return exceptions
    }

    private fun remapMod(file: File, mod: ForgeMod): List<Exception> {
        val exceptions = mutableListOf<Exception>()

        val hash = DigestUtils.md5Hex(file.inputStream())
        val modifiedJarFile = File(remappedModsDir, "${mod.modInfo.mod.modId}_$hash.jar")

        if (modifiedJarFile.exists() && !forceRemap) {
            mod.remappedModFile = modifiedJarFile
            return exceptions
        }

        val jar = JarFile(file)
        val output = modifiedJarFile.outputStream()
        val jarOutput = JarOutputStream(output)

        for (entry in jar.entries()) {
            if (!entry.name.endsWith(".class")) {
                if (entry.name.lowercase() == "manifest.mf") {
                    // Modify the manifest to avoid hash checking, because if
                    // hash checking occurs, the JAR will fail to load entirely.
                    val manifest = Manifest(jar.getInputStream(entry))

                    val hashes = mutableListOf<String>()
                    manifest.entries.forEach { (name, attr) ->
                        if (attr.entries.any { it.toString().startsWith("SHA-256-Digest") || it.toString().startsWith("SHA-1-Digest") }) {
                            hashes.add(name)
                        }
                    }

                    hashes.forEach {
                        manifest.entries.remove(it)
                    }

                    val outputStream = ByteArrayOutputStream()
                    manifest.write(outputStream)

                    jarOutput.putNextEntry(entry)
                    jarOutput.write(outputStream.toByteArray())
                    jarOutput.closeEntry()

                    continue
                } else if (entry.name.lowercase().endsWith(".rsa") || entry.name.lowercase().endsWith(".sf")) {
                    // ignore signed JARs
                    continue
                } else if (entry.name.lowercase().endsWith("refmap.json")) {
                    val refmapData = JsonParser.parseString(String(jar.getInputStream(entry).readAllBytes())).asJsonObject

                    val refmapMappings = refmapData.getAsJsonObject("mappings")
                    val newMappings = JsonObject()

                    refmapMappings.keySet().forEach { className ->
                        val mapped = refmapMappings.getAsJsonObject(className)
                        val properMapped = JsonObject()

                        mapped.entrySet().forEach { (name, element) ->
                            val srgMappedString = element.asString
                            val srgClass = srgMappedString.replaceAfter(";", "")
                            val intermediaryClass = remapDescriptor(srgClass)

                            if (srgMappedString.contains(":")) {
                                // field

                                val split = srgMappedString.split(":")
                                val srgField = split[0].removePrefix("L$srgClass;")
                                val srgDesc = split[1]

                                val intermediaryField = fieldMappings[srgField]?.first ?: srgField
                                val intermediaryDesc = remapDescriptor(srgDesc)

                                properMapped.addProperty(name, "$intermediaryClass$intermediaryField:$intermediaryDesc")
                            } else {
                                // method

                                val srgMethod = srgMappedString.replaceAfter("(", "").removeSuffix("(").removePrefix(srgClass)
                                val srgDesc = srgMappedString.replaceBefore("(", "")

                                val intermediaryMethod = methodMappings[srgMethod]?.first ?: srgMethod
                                val intermediaryDesc = remapDescriptor(srgDesc)

                                properMapped.addProperty(name, "$intermediaryClass$intermediaryMethod$intermediaryDesc")
                            }
                        }

                        newMappings.add(className, properMapped)
                    }

                    refmapData.add("mappings", newMappings)
                    refmapData.add("data", JsonObject().apply {
                        this.add("named:intermediary", newMappings)
                    })

                    jarOutput.putNextEntry(entry)
                    jarOutput.write(refmapData.toString().toByteArray())
                    jarOutput.closeEntry()

                    continue
                }

                jarOutput.putNextEntry(entry)
                jarOutput.write(jar.getInputStream(entry).readAllBytes())
                jarOutput.closeEntry()
                continue
            }

            val classReader = ClassReader(jar.getInputStream(entry))

            // we need the info for this for the class writer
            val classNode = ClassNode(Opcodes.ASM9)
            classReader.accept(classNode, 0)

            EventClassVisibilityFixer.fixClass(classNode)
            CommonSuperFixer.fixClass(classNode)
            ObjectHolderDefinalizer.processClass(classNode)

            try {
                val classWriter = ClassWriter(0)

                val visitor = ClassRemapper(classWriter, remapper)
                classNode.accept(visitor)

                jarOutput.putNextEntry(JarEntry(entry.name))
                jarOutput.write(classWriter.toByteArray())
                jarOutput.closeEntry()
            } catch (e: Exception) {
                logger.error("Failed to remap class ${classNode.name}!")
                e.printStackTrace()

                exceptions.add(e)
            }
        }

        jarOutput.close()
        mod.remappedModFile = modifiedJarFile

        return exceptions
    }

    fun remapClass(name: String): String {
        val workaround = kiltWorkaroundTree.classes.firstOrNull { it.getRawName("forge") == name }?.getRawName("kilt")

        return workaround ?: classMappings[name] ?: name
    }

    fun unmapClass(name: String): String {
        return classMappings.entries.firstOrNull { it.value == name }?.key ?: name
    }

    fun remapDescriptor(descriptor: String, reverse: Boolean = false): String {
        var formedString = ""

        var incompleteString = ""
        var isInClass = false
        descriptor.forEach {
            if (it == 'L' && !isInClass)
                isInClass = true

            if (isInClass) {
                incompleteString += it

                if (it == ';') {
                    isInClass = false

                    formedString += 'L'

                    val name = incompleteString.removePrefix("L").removeSuffix(";")
                    formedString += if (!reverse)
                        remapClass(name)
                    else
                        unmapClass(name)

                    formedString += ';'

                    incompleteString = ""
                }
            } else {
                formedString += it
            }
        }

        return formedString
    }
}