package xyz.bluspring.kilt.loader.remap

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.game.GameProviderHelper
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.fabricmc.loader.impl.util.SystemProperties
import net.fabricmc.mapping.tree.TinyMappingFactory
import net.fabricmc.mapping.tree.TinyTree
import net.fabricmc.mapping.util.AsmRemapperFactory
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.tree.ClassNode
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.mod.ForgeMod
import xyz.bluspring.kilt.loader.remap.fixers.EventClassVisibilityFixer
import xyz.bluspring.kilt.loader.remap.fixers.EventEmptyInitializerFixer
import xyz.bluspring.kilt.util.KiltHelper
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import kotlin.io.path.absolutePathString

object KiltRemapper {
    // Keeps track of the remapper changes, so every time I update the remapper,
    // it remaps all the mods following the remapper changes.
    // this can update by like 12 versions in 1 update, so don't worry too much about it.
    const val REMAPPER_VERSION = 103

    private val logger = LoggerFactory.getLogger("Kilt Remapper")
    // This is created automatically using https://github.com/BluSpring/srg2intermediary
    // srg -> intermediary
    val srgIntermediaryTree: TinyTree = TinyMappingFactory.load(this::class.java.getResourceAsStream("/srg_intermediary.tiny")!!.bufferedReader())
    private val kiltWorkaroundTree = TinyMappingFactory.load(this::class.java.getResourceAsStream("/kilt_workaround_mappings.tiny")!!.bufferedReader())

    // Mainly for debugging, so already-remapped Forge mods will be remapped again.
    private val forceRemap = System.getProperty("kilt.forceRemap")?.lowercase() == "true"

    // Mainly for debugging, used to test unobfuscated mods and ensure that Kilt is running as intended.
    private val disableRemaps = System.getProperty("kilt.noRemap")?.lowercase() == "true"

    // Generates local mappings file, you can use this if you're having trouble with the local ones.
    private val generateLocalMappingCache = System.getProperty("kilt.genLocalMapping")?.lowercase() == "true"

    private val launcher = FabricLauncherBase.getLauncher()
    internal val useNamed = launcher.targetNamespace != "intermediary"

    val mcRemapper = AsmRemapperFactory(srgIntermediaryTree).getRemapper("searge", "intermediary")
    private val mappingResolver = FabricLoader.getInstance().mappingResolver
    private val namespace: String = if (useNamed) launcher.targetNamespace else "intermediary"

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

        // Sort according to what dependencies are required and should be loaded first.
        // If a mod fails to remap because a dependency isn't listed, welp,
        // that's their problem now i guess.
        modRemapQueue.sortWith { a, b ->
            if (a.dependencies.any { it.modId == b.modId })
                1
            else if (b.dependencies.any { it.modId == a.modId })
                -1
            else 0
        }

        logger.info("Remapping Forge mods...")

        // Trying to see if we can multi-thread remapping, so it can be much faster.
        runBlocking {
            val modRemappingCoroutines = mutableMapOf<ForgeMod, Deferred<ForgeMod>>()

            modRemapQueue.forEach { mod ->
                if (mod.modFile == null)
                    return@forEach

                if (mod.isRemapped())
                    return@forEach

                modRemappingCoroutines[mod] = (async {
                    if (mod.isRemapped())
                        return@async mod

                    try {
                        val startTime = System.currentTimeMillis()
                        logger.info("Remapping ${mod.displayName} (${mod.modId})")

                        exceptions.addAll(remapMod(mod.modFile, mod,
                            // Get all the mod dependencies
                            recursiveListDependencies(mod, modLoadingQueue)
                        ))

                        logger.info("Remapped ${mod.displayName} (${mod.modId}) [took ${System.currentTimeMillis() - startTime}ms]")
                    } catch (e: Exception) {
                        exceptions.add(e)
                        e.printStackTrace()
                    }

                    mod
                })
            }

            awaitAll(*modRemappingCoroutines.values.toTypedArray())
        }

        logger.info("Finished remapping mods!")

        if (exceptions.isNotEmpty()) {
            logger.error("Ran into some errors, we're not going to continue with the repairing process.")
            return exceptions
        }

        return exceptions
    }

    private fun recursiveListDependencies(mod: ForgeMod, modLoadingQueue: ConcurrentLinkedQueue<ForgeMod>): MutableList<ForgeMod> {
        val list = mutableListOf<ForgeMod>()

        list.addAll(mod.dependencies.mapNotNull {
            modLoadingQueue.firstOrNull { a -> a.modId == it.modId }
        })

        val a = mutableListOf<ForgeMod>()
        list.forEach {
            a.addAll(recursiveListDependencies(it, modLoadingQueue))
        }

        list.addAll(a)

        return list
    }

    private fun remapMod(file: File, mod: ForgeMod, dependencies: List<ForgeMod>): List<Exception> {
        val exceptions = mutableListOf<Exception>()

        val hash = DigestUtils.md5Hex(file.inputStream())
        val modifiedJarFile = File(remappedModsDir, "${mod.modId}_${REMAPPER_VERSION}_$hash.jar")

        if (modifiedJarFile.exists() && !forceRemap) {
            mod.remappedModFile = modifiedJarFile
            return exceptions
        }

        val jar = JarFile(file)
        val output = modifiedJarFile.outputStream()
        val jarOutput = JarOutputStream(output)

        // Use the regular mod file
        val remapper = KiltAsmRemapper(dependencies.map { JarFile(it.modFile) }.toMutableList().apply {
            this.add(JarFile(mod.modFile))
        })

        for (entry in jar.entries()) {
            if (!entry.name.endsWith(".class")) {
                // JAR validation information stripping.
                // If we can find out how to use this to our advantage prior to remapping,
                // we may still be able to utilize this information safely.
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
                    // ignore JAR signatures.
                    // Due to Kilt remapping the JAR files, we are unable to use this to our advantage.
                    // TODO: Maybe run a verification step in the mod loading process prior to remapping?
                    logger.warn("Detected that ${mod.displayName} (${mod.modId}) is a signed JAR! This is a security measure by mod developers to verify that the distributed mod JARs are theirs, however Kilt is unable to use this verification step properly, and is thus stripping this information.")

                    continue
                }

                // Mixin remapping
                if (entry.name.lowercase().endsWith("refmap.json")) {
                    val refmapData = JsonParser.parseString(String(jar.getInputStream(entry).readAllBytes())).asJsonObject

                    val refmapMappings = refmapData.getAsJsonObject("mappings")
                    val newMappings = JsonObject()

                    refmapMappings.keySet().forEach { className ->
                        val mapped = refmapMappings.getAsJsonObject(className)
                        val properMapped = JsonObject()

                        mapped.entrySet().forEach { (name, element) ->
                            val srgMappedString = element.asString
                            val srgClass = if (srgMappedString.startsWith("L"))
                                srgMappedString.replaceAfter(";", "")
                            else
                                ""
                            val intermediaryClass = remapDescriptor(srgClass)

                            if (srgMappedString.contains(":")) {
                                // field

                                val split = srgMappedString.split(":")
                                val srgField = split[0].removePrefix(srgClass)
                                val srgDesc = split[1]

                                val intermediaryDesc = remapDescriptor(srgDesc)
                                val intermediaryField = mappingResolver.mapFieldName("intermediary",
                                    intermediaryClass.replace("/", "."),
                                    srgIntermediaryTree.classes.firstOrNull {
                                        it.getName("searge") == srgClass
                                    }?.fields?.firstOrNull {
                                        it.getName("searge") == srgField
                                    }?.getName("intermediary") ?: srgField,
                                    intermediaryDesc
                                )

                                properMapped.addProperty(name, "$intermediaryClass$intermediaryField:$intermediaryDesc")
                            } else {
                                // method

                                val srgMethod = srgMappedString.replaceAfter("(", "").removeSuffix("(").removePrefix(srgClass)
                                val srgDesc = srgMappedString.replaceBefore("(", "")

                                val intermediaryDesc = remapDescriptor(srgDesc)
                                val intermediaryMethod = mappingResolver.mapMethodName("intermediary",
                                    intermediaryClass.replace("/", "."),
                                    srgIntermediaryTree.classes.firstOrNull {
                                        it.getName("searge") == srgClass
                                    }?.methods?.firstOrNull {
                                        it.getName("searge") == srgMethod
                                    }?.getName("intermediary") ?: srgMethod,
                                    intermediaryDesc
                                )

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
                    jarOutput.write(Kilt.gson.toJson(refmapData).toByteArray())
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
            EventEmptyInitializerFixer.fixClass(classNode)
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

    fun remapClass(name: String, toIntermediary: Boolean = false, ignoreWorkaround: Boolean = false): String {
        val workaround = if (!ignoreWorkaround)
            kiltWorkaroundTree.classes.firstOrNull { it.getRawName("forge") == name }?.getRawName("kilt")
        else null
        val intermediary = mcRemapper.map(name)

        if (toIntermediary) {
            return workaround ?: intermediary ?: name
        }

        return (workaround ?: if (intermediary != null)
            mappingResolver.mapClassName("intermediary", intermediary.replace("/", ".")) ?: name
        else name).replace(".", "/")
    }

    fun unmapClass(name: String): String {
        val intermediary = mappingResolver.unmapClassName("intermediary", name.replace("/", "."))
        return srgIntermediaryTree.classes.firstOrNull { it.getName("intermediary") == intermediary }?.getName("searge") ?: name
    }

    val gameJar = getMCGameJar()

    fun getKiltClassNode(className: String): ClassNode? {
        if (!className.startsWith("net/minecraftforge/") && !className.startsWith("xyz/bluspring/kilt/"))
            return null

        KiltHelper.getForgeClassNodes().forEach {
            if (it.name == className)
                return it
        }

        return null
    }

    fun getGameClassNode(className: String): ClassNode? {
        if (gameJar == null)
            return null

        if (className.startsWith("net/minecraftforge/") || className.startsWith("xyz/bluspring/kilt/"))
            return getKiltClassNode(className)

        if (!className.startsWith("com/mojang/") && !className.startsWith("net/minecraft/"))
            return null

        val entry = gameJar.getJarEntry("$className.class")

        if (entry != null) {
            val classReader = ClassReader(gameJar.getInputStream(entry))
            val classNode = ClassNode(Opcodes.ASM9)
            classReader.accept(classNode, 0)

            return classNode
        }

        return null
    }

    private fun getMCGameJar(): JarFile? {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment) {
            val commonJar = GameProviderHelper.getCommonGameJar()

            if (commonJar != null)
                return JarFile(commonJar.toFile())

            val sidedJar = GameProviderHelper.getEnvGameJar(FabricLoader.getInstance().environmentType)

            if (sidedJar != null)
                return JarFile(sidedJar.toFile())
        } else {
            // TODO: is there a better way of doing this?
            val possibleMcGameJar = FabricLauncherBase.getLauncher().classPath.firstOrNull { path ->
                val str = path.absolutePathString()
                str.contains("net") && str.contains("minecraft") && str.contains("-loom.mappings.") && str.contains("minecraft-merged-")
            } ?: return null

            return JarFile(possibleMcGameJar.toFile())
        }

        return null
    }

    private fun getMCGameClassPath(): Array<out Path> {
        return if (!FabricLoader.getInstance().isDevelopmentEnvironment)
            arrayOf(FabricLoader.getInstance().objectShare.get("fabric-loader:inputGameJar") as Path)
        else
            mutableListOf<Path>().apply {
                val remapClasspathFile = System.getProperty(SystemProperties.REMAP_CLASSPATH_FILE)
                    ?: throw RuntimeException("No remapClasspathFile provided")

                val content = String(Files.readAllBytes(Paths.get(remapClasspathFile)), StandardCharsets.UTF_8)

                this.addAll(Arrays.stream(content.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray())
                    .map { first ->
                        Paths.get(first)
                    }
                    .toList())
            }.toTypedArray()
    }

    fun remapDescriptor(descriptor: String, reverse: Boolean = false, toIntermediary: Boolean = false): String {
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
                        remapClass(name, toIntermediary)
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