package xyz.bluspring.kilt.loader.remap

import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.fabricmc.loader.impl.util.SystemProperties
import net.fabricmc.mapping.tree.TinyMappingFactory
import net.fabricmc.mapping.tree.TinyTree
import net.fabricmc.tinyremapper.IMappingProvider
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.ForgeMod
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.kilt.loader.staticfix.StaticAccessFixer
import xyz.bluspring.kilt.loader.superfix.CommonSuperClassWriter
import xyz.bluspring.kilt.loader.superfix.CommonSuperFixer
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Function
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.stream.Collectors
import kotlin.io.path.name
import kotlin.io.path.toPath

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

        val srgMappedMinecraft = remapMinecraft()
        val gameClassPath = getGameClassPath()

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
                remapMod(mod.modFile, mod, arrayOf(
                    srgMappedMinecraft,
                    *gameClassPath,
                    *modRemapQueue.filter { mod.modInfo.mod.dependencies.any { dep -> it.modInfo.mod.modId == dep.modId } }
                        .map { it.remappedModFile.toPath() }.toTypedArray()
                ))
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

    private fun remapMod(file: File, mod: ForgeMod, gameClassPath: Array<out Path>) {
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

            val visitor = KiltRemapperVisitor(srgIntermediaryTree, kiltWorkaroundTree, classNode)
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

    private val targetNamespace = FabricLauncherBase.getLauncher().targetNamespace

    private fun getGameClassPath(): Array<out Path> {
        return if (!FabricLoader.getInstance().isDevelopmentEnvironment)
            mutableListOf<Path>().apply {
                this.addAll(FabricLauncherBase.getLauncher().classPath)
                this.add(Kilt::class.java.protectionDomain.codeSource.location.toURI().toPath())
            }.toTypedArray()
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
                    .collect(Collectors.toList()))

                this.add(Kilt::class.java.protectionDomain.codeSource.location.toURI().toPath())
            }.toTypedArray()
    }

    fun remapMinecraft(): Path {
        val launcher = FabricLauncherBase.getLauncher()
        val srgFile = File(KiltLoader.kiltCacheDir, "minecraft_${launcher.mappingConfiguration.gameVersion}-srg.jar")

        if (srgFile.exists())
            return srgFile.toPath()

        logger.info("Creating SRG-mapped Minecraft JAR for remapping Forge mods...")
        //val srgIntermediaryMappings = IMappingFile.load(this::class.java.getResourceAsStream("/srg_intermediary.tiny")!!)

        val minecraftPath = FabricLoader.getInstance().objectShare.get("fabric-loader:inputGameJar") as Path
        val intermediaryPath = if (FabricLoader.getInstance().isDevelopmentEnvironment) { // named
            // I think this is the best bet I have to finding an Intermediary jar in dev
            getGameClassPath().first { it.name.contains("intermediary") }
        } else minecraftPath // intermediary, or well should be.

        val srgRemapper = createRemapper(createMappings(srgIntermediaryTree, "intermediary", "searge")).build()

        srgRemapper.readInputs(intermediaryPath)
        val outputConsumer = OutputConsumerPath.Builder(srgFile.toPath()).apply {
            assumeArchive(true)
        }.build()

        srgRemapper.apply(outputConsumer)

        srgRemapper.finish()
        outputConsumer.close()

        logger.info("Remapped Minecraft from Intermediary to SRG.")

        return srgFile.toPath()
    }

    private fun createMappings(tree: TinyTree, from: String, to: String): IMappingProvider {
        return IMappingProvider { out ->
            tree.classes.forEach { classDef ->
                out.acceptClass(classDef.getName(from), classDef.getName(to))

                classDef.fields.forEach { fieldDef ->
                    out.acceptField(IMappingProvider.Member(classDef.getName(from), fieldDef.getName(from), fieldDef.getDescriptor(to)), fieldDef.getName(to))
                }

                classDef.methods.forEach { methodDef ->
                    out.acceptMethod(IMappingProvider.Member(classDef.getName(from), methodDef.getName(from), methodDef.getDescriptor(to)), methodDef.getName(to))
                }
            }
        }
    }

    private fun createRemapper(provider: IMappingProvider): TinyRemapper.Builder {
        return TinyRemapper.newRemapper().apply {
            renameInvalidLocals(false)
            fixPackageAccess(true)

            withMappings(provider)
        }
    }
}