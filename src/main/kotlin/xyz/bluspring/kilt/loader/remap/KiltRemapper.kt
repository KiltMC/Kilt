package xyz.bluspring.kilt.loader.remap

import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.fabricmc.mapping.tree.TinyMappingFactory
import net.fabricmc.mapping.tree.TinyTree
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.ForgeMod
import xyz.bluspring.kilt.loader.staticfix.StaticAccessFixer
import xyz.bluspring.kilt.loader.superfix.CommonSuperClassWriter
import xyz.bluspring.kilt.loader.superfix.CommonSuperFixer
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Function
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

object KiltRemapper {
    private val logger = Kilt.logger
    // This is created automatically using https://github.com/BluSpring/srg2intermediary
    // srg -> intermediary
    val srgIntermediaryTree: TinyTree = TinyMappingFactory.load(this::class.java.getResourceAsStream("/srg_intermediary.tiny")!!.bufferedReader())
    private val kiltWorkaroundTree = TinyMappingFactory.load(this::class.java.getResourceAsStream("/kilt_workaround_mappings.tiny")!!.bufferedReader())

    // Mainly for debugging, so already-remapped Forge mods will be remapped again.
    private val forceRemap = System.getProperty("kilt.forceRemap")?.lowercase() == "true"

    // Mainly for debugging, used to test unobfuscated mods and ensure that Kilt is running as intended.
    private val disableRemaps = System.getProperty("kilt.noRemap")?.lowercase() == "true"

    // SRG class -> Intermediary/Named class
    private val classMappings = mutableMapOf<String, String>()

    // SRG field -> Intermediary/Named name + descriptor
    private val fieldMappings = mutableMapOf<String, Pair<String, String>>()

    // SRG method name + descriptor -> Intermediary/Named name + descriptor
    private val methodMappings = mutableMapOf<Pair<String, String>, Pair<String, String>>()

    private val launcher = FabricLauncherBase.getLauncher()
    internal val useNamed = launcher.targetNamespace != "intermediary"

    private val namespace: String
        get() {
            return if (useNamed)
                launcher.targetNamespace
            else "intermediary"
        }

    init {
        val mappings = FabricLauncherBase.getLauncher().mappingConfiguration.mappings

        srgIntermediaryTree.classes.forEach { srgClass ->
            val intermediaryClass = mappings.classes.first { it.getName("intermediary") == srgClass.getName("intermediary") }

            classMappings[srgClass.getName("searge")] = intermediaryClass.getName(namespace)

            srgClass.fields.forEach field@{ srgField ->
                val intermediaryField = intermediaryClass.fields.firstOrNull { it.getName("intermediary") == srgField.getName("intermediary") }

                if (intermediaryField == null) {
                    fieldMappings[srgField.getName("searge")] = Pair(srgField.getName("intermediary"), srgField.getDescriptor("intermediary"))
                    return@field
                }

                fieldMappings[srgField.getName("searge")] = Pair(intermediaryField.getName(namespace), intermediaryField.getDescriptor(
                    namespace
                ))
            }

            srgClass.methods.forEach method@{ srgMethod ->
                // need to use a different way of getting the method, because SRG stores method members in literally everyone
                val intermediaryClass2 = mappings.classes.firstOrNull { it.methods.any { m -> m.getName("intermediary") == srgMethod.getName("intermediary") && m.getDescriptor("intermediary") == srgMethod.getDescriptor("intermediary") } }

                if (intermediaryClass2 == null) {
                    methodMappings[Pair(srgMethod.getName("searge"), srgMethod.getDescriptor("searge"))] = Pair(srgMethod.getName("intermediary"), srgMethod.getDescriptor("intermediary"))
                    return@method
                }

                val intermediaryMethod = intermediaryClass2.methods.first { it.getName("intermediary") == srgMethod.getName("intermediary") && it.getDescriptor("intermediary") == srgMethod.getDescriptor("intermediary") }

                methodMappings[Pair(srgMethod.getName("searge"), srgMethod.getDescriptor("searge"))] = Pair(intermediaryMethod.getName(
                    namespace
                ), intermediaryMethod.getDescriptor(namespace))
            }
        }
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
                remapMod(mod.modFile, mod)
                logger.info("Remapped ${mod.modInfo.mod.displayName} (${mod.modInfo.mod.modId})")
            } catch (e: Exception) {
                exceptions.add(e)
                e.printStackTrace()
            }
        }

        logger.info("Finished remapping mods!")

        StaticAccessFixer.fixMods(modLoadingQueue, remappedModsDir)
        CommonSuperFixer.fixMods(modLoadingQueue, remappedModsDir)

        return exceptions
    }

    private fun remapMod(file: File, mod: ForgeMod) {
        val hash = DigestUtils.md5Hex(file.inputStream())
        val modifiedJarFile = File(remappedModsDir, "${mod.modInfo.mod.modId}_$hash.jar")

        if (modifiedJarFile.exists() && !forceRemap) {
            mod.remappedModFile = modifiedJarFile
            return
        }

        val jar = JarFile(file)
        val output = modifiedJarFile.outputStream()
        val jarOutput = JarOutputStream(output)

        for (entry in jar.entries()) {
            if (!entry.name.endsWith(".class")) {
                jarOutput.putNextEntry(entry)
                jarOutput.write(jar.getInputStream(entry).readAllBytes())
                jarOutput.closeEntry()
                continue
            }

            val classNode = ClassNode(Opcodes.ASM9)
            val classReader = ClassReader(jar.getInputStream(entry))

            classReader.accept(classNode, 0)

            val visitor = KiltRemapperVisitor(
                srgIntermediaryTree, kiltWorkaroundTree, classNode,
                classMappings, fieldMappings, methodMappings
            )
            val modifiedClass = visitor.write()

            val classWriter = CommonSuperClassWriter.createClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS, classNode, Function {
                val classEntry = jar.getJarEntry("${it.replace(".", "/")}.class")
                return@Function if (classEntry == null)
                    null
                else
                    jar.getInputStream(classEntry).readAllBytes()
            })
            modifiedClass.accept(classWriter)

            jarOutput.putNextEntry(JarEntry(entry.name))
            jarOutput.write(classWriter.toByteArray())
            jarOutput.closeEntry()
        }

        jarOutput.close()
        mod.remappedModFile = modifiedJarFile
    }
}