package xyz.bluspring.kilt.loader.remap

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import it.unimi.dsi.fastutil.objects.Object2ReferenceFunction
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.stream.consumeAsFlow
import kotlinx.coroutines.withContext
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.game.GameProviderHelper
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.fabricmc.loader.impl.util.SystemProperties
import net.fabricmc.mapping.tree.TinyMappingFactory
import net.minecraftforge.fart.api.ClassProvider
import net.minecraftforge.fart.internal.EnhancedClassRemapper
import net.minecraftforge.fart.internal.EnhancedRemapper
import net.minecraftforge.fart.internal.RenamingTransformer
import net.minecraftforge.srgutils.IMappingFile
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.slf4j.LoggerFactory
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.kilt.loader.mod.ForgeMod
import xyz.bluspring.kilt.loader.remap.fixers.ConflictingStaticMethodFixer
import xyz.bluspring.kilt.loader.remap.fixers.EventClassVisibilityFixer
import xyz.bluspring.kilt.loader.remap.fixers.EventEmptyInitializerFixer
import xyz.bluspring.kilt.loader.remap.fixers.MixinShadowRemapper
import xyz.bluspring.kilt.loader.remap.fixers.MixinSpecialAnnotationRemapper
import xyz.bluspring.kilt.loader.remap.fixers.WorkaroundFixer
import xyz.bluspring.kilt.util.CaseInsensitiveStringHashSet
import xyz.bluspring.kilt.util.ClassNameHashSet
import xyz.bluspring.kilt.util.KiltHelper
import xyz.bluspring.kilt.util.filterAsync
import xyz.bluspring.kilt.util.flatMapAsync
import xyz.bluspring.kilt.util.mapAsync
import xyz.bluspring.kilt.util.onEachAsync
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createFile
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import kotlin.io.path.toPath
import kotlin.time.measureTime


object KiltRemapper {
    // Keeps track of the remapper changes, so every time I update the remapper,
    // it remaps all the mods following the remapper changes.
    // this can update by like 12 versions in 1 update, so don't worry too much about it.
    const val REMAPPER_VERSION = 138

    const val MC_MAPPED_JAR_VERSION = 2

    val logConsumer = Consumer<String> {
        logger.debug(it)
    }

    private val logger = LoggerFactory.getLogger("Kilt Remapper")

    private val launcher = FabricLauncherBase.getLauncher()
    internal val useNamed = launcher.targetNamespace != "intermediary"

    // Mainly for debugging, to make sure all Forge mods remap correctly in production environments
    // without needing to actually launch a production environment.
    internal val forceProductionRemap = System.getProperty("kilt.forceProductionRemap")?.lowercase() == "true"

    // This is created automatically using https://github.com/BluSpring/srg2intermediary
    // srg -> intermediary
    val srgIntermediaryMapping = IMappingFile.load(this::class.java.getResourceAsStream("/srg_intermediary.tiny")!!)
        .run {
            if (!forceProductionRemap)
                this.rename(DevMappingRenamer())
            else
                this
        }
    val intermediarySrgMapping = srgIntermediaryMapping.reverse()

    // Some workaround mappings, to remap some names to Kilt equivalents.
    // This fixes some compatibility issues.
    private val kiltWorkaroundTree = TinyMappingFactory.load(
        this::class.java.getResourceAsStream("/kilt_workaround_mappings.tiny")!!.bufferedReader()
    )

    // Mainly for debugging, so already-remapped Forge mods will be remapped again.
    private val forceRemap = System.getProperty("kilt.forceRemap")?.lowercase() == "true"

    // Mainly for debugging, used to test unobfuscated mods and ensure that Kilt is running as intended.
    private val disableRemaps = System.getProperty("kilt.noRemap")?.lowercase() == "true"

    private val mappingResolver =
        if (forceProductionRemap) NoopMappingResolver() else FabricLoader.getInstance().mappingResolver
    private val namespace: String = if (useNamed) launcher.targetNamespace else "intermediary"

    private lateinit var remappedModsDir: Path

    // SRG name -> (parent class name, intermediary/mapped name)
    val srgMappedFields = runBlocking {
        srgIntermediaryMapping.classes.asFlow().flatMapAsync {
            it.fields.asFlow().mapAsync { f ->
                f.original to
                        if (!forceProductionRemap)
                            mappingResolver.mapFieldName(
                                "intermediary",
                                it.mapped.replace("/", "."),
                                f.mapped,
                                f.mappedDescriptor
                            )
                        else
                            f.mapped
            }
        }.toSet().associateBy { it.first }
    }

    // SRG name -> (parent class name, intermediary/mapped name)
    val srgMappedMethods =
        Object2ReferenceMaps.synchronize(Object2ReferenceOpenHashMap<String, MutableMap<String, String>>())

    init {
        srgIntermediaryMapping.classes.asFlow().flowOn(Dispatchers.IO).onEachAsync {
            it.methods.forEach m@{ f ->
                // otherwise FunctionalInterface methods don't get remapped properly???
                if (!f.mapped.startsWith("method_") && !FabricLoader.getInstance().isDevelopmentEnvironment)
                    return@m

                val map = srgMappedMethods.getOrPut(f.original) {
                    Object2ReferenceMaps.synchronize(Object2ReferenceOpenHashMap())
                }
                val mapped = if (!forceProductionRemap)
                    (mappingResolver.mapMethodName(
                        "intermediary",
                        it.mapped.replace("/", "."),
                        f.mapped,
                        f.mappedDescriptor
                    ))
                else
                    f.mapped

                map[f.parent.original] = mapped
            }
        }.launchIn(Kilt.loader.scope)
    }

    suspend fun remapMods(modLoadingQueue: ConcurrentLinkedQueue<ForgeMod>, remappedModsDir: Path): List<Exception> {
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

        srgGamePath = remapMinecraft()

        val exceptions = mutableListOf<Exception>()

        val mods = modLoadingQueue.filterNot { it.isRemapped() || it.modFile == null }
        val modById = mods.associateBy { it.modId }

        val modToJob = Object2ReferenceMaps.synchronize(Object2ReferenceOpenHashMap<String, Job>())

        logger.info("Remapping Forge mods...")

        suspend fun remapMod(file: Path, mod: ForgeMod, forgeModsList: Collection<ForgeMod>): List<Exception> {
            val exceptions = mutableListOf<Exception>()

            val hash = withContext(Dispatchers.IO) { DigestUtils.md5Hex(file.inputStream()) }
            val modifiedJarFile = KiltRemapper.remappedModsDir / "${mod.modId}_${REMAPPER_VERSION}_$hash.jar"

            if (modifiedJarFile.exists() && !forceRemap) {
                mod.remappedModFile = modifiedJarFile.toFile()
                return emptyList()
            }

            val jar = withContext(Dispatchers.IO) { JarFile(file.toFile()) }
            val output = modifiedJarFile.outputStream()
            val jarOutput = withContext(Dispatchers.IO) { JarOutputStream(output) }

            suspend fun ClassProvider.Builder.addCommonLibraries(forgeModsList: Collection<ForgeMod>) {
                this.addLibrary(srgGamePath)

                // List down Forge paths
                for (path in KiltHelper.getKiltPaths()) {
                    this.addLibrary(path)
                }

                // Add all Fabric mods
                for (container in FabricLoader.getInstance().allMods) {
                    for (rootPath in container.rootPaths) {
                        this.addLibrary(rootPath)
                    }
                }

                // add mapped path too
                for (path in getGameClassPath()) {
                    this.addLibrary(path)
                }

                // Add all Forge mods to the library path, because dependencies don't have to be specified
                // in order to use mods lmao
                for (forgeMod in forgeModsList) {
                    this.addLibrary(forgeMod.modFile?.toPath())
                }
            }

            // Use the regular mod file
            val classProvider = ClassProvider.builder().apply {
                this.addCommonLibraries(forgeModsList)
                this.addLibrary(mod.modFile?.toPath())
            }.build()

            val remapper = KiltEnhancedRemapper(classProvider, srgIntermediaryMapping, logConsumer)
            val entryToClassNodes = Object2ReferenceMaps.synchronize(Object2ReferenceOpenHashMap<JarEntry, ClassNode>())

            val mixinClasses = ClassNameHashSet()
            val refmaps = CaseInsensitiveStringHashSet()

            suspend fun processManifest(
                jar: JarFile,
                manifestEntry: JarEntry,
                jarOutput: JarOutputStream
            ) = withContext(Dispatchers.IO) {
                // Modify the manifest to avoid hash checking, because if
                // hash checking occurs, the JAR will fail to load entirely.
                val manifest =
                    Manifest(jar.getInputStream(manifestEntry))

                manifest.entries.keys.removeIf { it == "SHA-256-Digest" || it == "SHA-1-Digest" }

                synchronized(jarOutput) {
                    jarOutput.putNextEntry(manifestEntry)
                    jarOutput.write(ByteArrayOutputStream().also { manifest.write(it) }.toByteArray())
                    jarOutput.closeEntry()
                }

                return@withContext manifest
            }

            // JAR validation information stripping.
            // If we can find out how to use this to our advantage prior to remapping,
            // we may still be able to use this information safely.
            val manifestEntry = jar.getJarEntry("META-INF/MANIFEST.MF")
            if (manifestEntry != null) {
                val manifest = processManifest(jar, manifestEntry, jarOutput)

                suspend fun processMixinConfigs(
                    manifest: Manifest,
                    jar: JarFile,
                    mixinClasses: MutableSet<String>,
                    refmaps: MutableSet<String>
                ) {
                    val mixinConfigs = manifest.mainAttributes.getValue("MixinConfigs")?.split(",") ?: listOf()

                    // Read mixin configs and add them to the list of mixins to fix
                    for (mixinConfig in mixinConfigs) {
                        val jsonEntry = jar.getJarEntry(mixinConfig) ?: continue
                        val data = withContext(Dispatchers.IO) { jar.getInputStream(jsonEntry) }.reader()

                        val json = JsonParser.parseReader(data).asJsonObject

                        if (!json.has("package"))
                            continue

                        val mixinPackage = json.get("package").asString

                        (json.get("mixins") as? JsonArray)?.forEach {
                            mixinClasses.add("$mixinPackage.${it.asString}")
                        }

                        (json.get("client") as? JsonArray)?.forEach {
                            mixinClasses.add("$mixinPackage.${it.asString}")
                        }

                        (json.get("server") as? JsonArray)?.forEach {
                            mixinClasses.add("$mixinPackage.${it.asString}")
                        }

                        runCatching { json.get("refmap")!!.asString }.onSuccess { refmaps.add(it) }
                    }
                }

                processMixinConfigs(manifest, jar, mixinClasses, refmaps)
            }

            suspend fun remapRefmap(
                jar: JarFile,
                entry: JarEntry,
                remapper: KiltEnhancedRemapper,
                jarOutput: JarOutputStream
            ) {
                val refmapData =
                    JsonParser.parseReader(withContext(Dispatchers.IO) { jar.getInputStream(entry) }.reader()).asJsonObject

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
                        val intermediaryClass = if (srgClass.isNotBlank()) remapDescriptor(
                            srgClass,
                            toIntermediary = forceProductionRemap
                        ) else ""

                        if (srgMappedString.contains(":")) {
                            // field

                            val split = srgMappedString.split(":")
                            val srgField = split[0].removePrefix(srgClass)
                            val srgDesc = split[1]

                            val intermediaryDesc = remapDescriptor(srgDesc, toIntermediary = forceProductionRemap)

                            val intermediaryField = "".run {
                                if (srgClass.isNotBlank()) {
                                    if (nameMappingCache.contains(srgField)) {
                                        nameMappingCache[srgField]!!
                                    } else {
                                        // Remap SRG to Intermediary, then to whatever the current FabricMC environment
                                        // is using.
                                        mappingResolver.mapFieldName(
                                            "intermediary",
                                            intermediaryClass
                                                .replace("/", ".")
                                                .removePrefix("L").removeSuffix(";"),
                                            (remapper.mapFieldName(
                                                srgClass.removePrefix("L").removeSuffix(";"),
                                                srgField,
                                                srgDesc
                                            ).run a@{
                                                if (this == srgField) {
                                                    val possibleClass = srgIntermediaryMapping.classes.firstOrNull {
                                                        it.getField(srgField) != null
                                                    } ?: return@run srgField

                                                    mappingResolver.mapFieldName(
                                                        "intermediary",
                                                        possibleClass.mapped.replace("/", "."),
                                                        possibleClass.remapField(srgField),
                                                        intermediaryDesc
                                                    )
                                                } else this
                                            }).apply {
                                                // Cache the field we found, so we don't have to go through this again
                                                nameMappingCache[srgField] = this
                                            } ?: srgField,
                                            intermediaryDesc
                                        )
                                    }
                                } else {
                                    // If the refmap is missing an owner class, try to figure it out
                                    if (!srgField.startsWith("f_") || !srgField.endsWith("_"))
                                        srgField // short-circuit if it doesn't look like a field
                                    else {
                                        if (nameMappingCache.contains(srgField))
                                            nameMappingCache[srgField]!!
                                        else {
                                            val possibleClass =
                                                srgIntermediaryMapping.classes.firstOrNull { it.getField(srgField) != null }
                                                    ?: return@run srgField

                                            mappingResolver.mapFieldName(
                                                "intermediary",
                                                possibleClass.mapped.replace("/", "."),
                                                possibleClass.remapField(srgField),
                                                intermediaryDesc
                                            ).apply {
                                                // Cache the field we found, so we don't have to go through this again
                                                nameMappingCache[srgField] = this
                                            }
                                        }
                                    }
                                }
                            }

                            properMapped.addProperty(name, "$intermediaryClass$intermediaryField:$intermediaryDesc")
                        } else {
                            // method

                            val srgMethod =
                                srgMappedString.replaceAfter("(", "").removeSuffix("(").removePrefix(srgClass)
                            val srgDesc = srgMappedString.replaceBefore("(", "")

                            val intermediaryDesc = remapDescriptor(srgDesc, toIntermediary = forceProductionRemap)
                            val intermediaryMethod = "".run {
                                if (srgClass.isNotBlank()) {
                                    if (nameMappingCache.contains(srgMethod)) {
                                        nameMappingCache[srgMethod]!!
                                    } else {
                                        mappingResolver.mapMethodName(
                                            "intermediary",
                                            intermediaryClass
                                                .replace("/", ".")
                                                .removePrefix("L").removeSuffix(";"),
                                            (remapper.mapMethodName(
                                                srgClass
                                                    .removePrefix("L").removeSuffix(";"),
                                                srgMethod, srgDesc
                                            ).run a@{
                                                if (this == srgMethod) {
                                                    val possibleClass = srgIntermediaryMapping.classes.firstOrNull {
                                                        it.getMethod(
                                                            srgMethod,
                                                            srgDesc
                                                        ) != null
                                                    } ?: return@a srgMethod

                                                    mappingResolver.mapMethodName(
                                                        "intermediary",
                                                        possibleClass.mapped.replace("/", "."),
                                                        possibleClass.remapMethod(srgMethod, srgDesc),
                                                        intermediaryDesc
                                                    )
                                                } else this
                                            }).apply {
                                                nameMappingCache[srgMethod] = this
                                            } ?: srgMethod,
                                            intermediaryDesc
                                        )
                                    }
                                } else {
                                    // If the refmap is missing an owner class, try to figure it out
                                    // Since record classes can provide methods with f_num_, these have to be
                                    // taken into account.
                                    if (!(srgMethod.startsWith("f_") || srgMethod.startsWith("m_")) || !srgMethod.endsWith(
                                            "_"
                                        )
                                    )
                                        srgMethod // short-circuit if it doesn't look like a method
                                    else {
                                        if (nameMappingCache.contains(srgMethod))
                                            nameMappingCache[srgMethod]!!
                                        else {
                                            val possibleClass = srgIntermediaryMapping.classes.firstOrNull {
                                                it.getMethod(
                                                    srgMethod,
                                                    srgDesc
                                                ) != null
                                            } ?: return@run srgMethod

                                            mappingResolver.mapMethodName(
                                                "intermediary",
                                                possibleClass.mapped.replace("/", "."),
                                                possibleClass.remapMethod(srgMethod, srgDesc),
                                                intermediaryDesc
                                            ).apply {
                                                // Cache the method we found, so we don't have to go through this again
                                                nameMappingCache[srgMethod] = this
                                            }
                                        }
                                    }
                                }
                            }

                            properMapped.addProperty(name, "$intermediaryClass$intermediaryMethod$intermediaryDesc")
                        }
                    }

                    newMappings.add(className, properMapped)
                }

                refmapData.add("mappings", newMappings)
                refmapData.add("data", JsonObject().apply {
                    this.add("named:intermediary", newMappings)
                })

                synchronized(jarOutput) {
                    jarOutput.putNextEntry(entry)
                    jarOutput.write(Kilt.gson.toJson(refmapData).toByteArray())
                    jarOutput.closeEntry()
                }

                return
            }

            jar.stream().consumeAsFlow()
                .flowOn(Dispatchers.IO)
                .filterAsync { !it.name.equals("META-INF/MANIFEST.MF", true) }
                .filterAsync {
                    val isHash = it.name.endsWith(".rsa", true) || it.name.endsWith(".sf", true)
                    if (isHash) {
                        // ignore JAR signatures.
                        // Due to Kilt remapping the JAR files, we are unable to use this to our advantage.
                        // TODO: Maybe run a verification step in the mod loading process prior to remapping?
                        logger.warn("Detected that ${mod.displayName} (${mod.modId}) is a signed JAR! This is a security measure by mod developers to verify that the distributed mod JARs are theirs, however Kilt is unable to use this verification step properly, and isthus stripping this information.")
                    }
                    !isHash
                }
                .onEachAsync { entry ->
                    when {
                        entry.name in refmaps -> remapRefmap(jar, entry, remapper, jarOutput)

                        // Keep the other resources
                        !entry.name.endsWith(".class") -> {
                            synchronized(jarOutput) {
                                jarOutput.putNextEntry(entry)
                                jarOutput.write(jar.getInputStream(entry).readAllBytes())
                                jarOutput.closeEntry()
                            }
                        }

                        else -> {
                            val classReader = ClassReader(jar.getInputStream(entry))

                            // we need the info for this for the class writer
                            val classNode = ClassNode(Opcodes.ASM9)
                            classReader.accept(classNode, 0)

                            entryToClassNodes[JarEntry(entry.name)] = classNode
                        }
                    }
                }
                .collect()

            val classesToProcess =
                entryToClassNodes
                    .values
                    .intersect(KiltHelper.getForgeClassNodes().toSet())
                    .toList()

            fun remapClass(
                remapper: KiltEnhancedRemapper,
                originalNode: ClassNode,
                mixinClasses: ClassNameHashSet,
                classesToProcess: List<ClassNode>,
                jarOutput: JarOutputStream,
                entry: JarEntry,
                exceptions: MutableList<Exception>
            ) {
                try {
                    val remappedNode = ClassNode(Opcodes.ASM9)

                    val visitor = EnhancedClassRemapper(remappedNode, remapper, RenamingTransformer(remapper, false))
                    originalNode.accept(visitor)

                    // only do this on mixin classes, please
                    if (remappedNode.name in mixinClasses) {
                        MixinShadowRemapper.remapClass(remappedNode)
                    }

                    EventClassVisibilityFixer.fixClass(remappedNode)
                    EventEmptyInitializerFixer.fixClass(remappedNode, classesToProcess)
                    ObjectHolderDefinalizer.processClass(remappedNode)
                    WorkaroundFixer.fixClass(remappedNode)
                    ConflictingStaticMethodFixer.fixClass(remappedNode)
                    MixinSpecialAnnotationRemapper.remapClass(remappedNode)

                    val classWriter = ClassWriter(0)
                    remappedNode.accept(classWriter)

                    synchronized(jarOutput) {
                        jarOutput.putNextEntry(entry)
                        jarOutput.write(classWriter.toByteArray())
                        jarOutput.closeEntry()
                    }
                } catch (e: Exception) {
                    logger.error("Failed to remap class ${entry.name}!", e)
                    exceptions.add(e)
                }
            }

            entryToClassNodes.asSequence().asFlow().flowOn(Dispatchers.IO)
                .onEachAsync { (entry, originalNode) ->
                    remapClass(remapper, originalNode, mixinClasses, classesToProcess, jarOutput, entry, exceptions)
                }.collect()

            mod.remappedModFile = modifiedJarFile.toFile()
            withContext(Dispatchers.IO) { jarOutput.close() }

            return exceptions
        }

        fun remapModAsync(
            mod: ForgeMod,
            mods: Collection<ForgeMod>
        ): Job = Kilt.loader.scope.launch(Dispatchers.IO, start = CoroutineStart.LAZY) {
            runCatching {
                mod.dependencies.mapNotNull {
                    if (modToJob[it.modId] == null)
                        modById[it.modId]?.let { it ->
                            remapModAsync(
                                it,
                                mods
                            )
                        }
                    else null
                }.joinAll()

                logger.info("Remapping ${mod.displayName} (${mod.modId})")
                val ms = measureTime {
                    exceptions.addAll(remapMod(mod.modFile!!.toPath(), mod, mods))
                }
                logger.info("Remapped ${mod.displayName} (${mod.modId}) [took ${ms}]")
            }.onFailure {
                logger.error("Failed to remap ${mod.displayName} (${mod.modId})", it)
                if (it is Exception) {
                    exceptions.add(it)
                }
            }
        }

        for (mod in mods) {
            modToJob.computeIfAbsent(mod.modId, Object2ReferenceFunction { remapModAsync(mod, mods) })
        }

        modToJob.values.joinAll()

        logger.info("Finished remapping mods!")

        if (exceptions.isNotEmpty()) {
            logger.error("Ran into some errors, we're not going to continue with the repairing process.")
            return exceptions
        }

        return exceptions
    }

    private val nameMappingCache = mutableMapOf<String, String>()

    fun remapClass(name: String, toIntermediary: Boolean = false, ignoreWorkaround: Boolean = false): String {
        val workaround = if (!ignoreWorkaround)
            kiltWorkaroundTree.classes.firstOrNull { it.getRawName("forge") == name }?.getRawName("kilt")
        else null
        val intermediary = srgIntermediaryMapping.remapClass(name.replace(".", "/"))
        if (toIntermediary) {
            return workaround ?: intermediary ?: name
        }

        return (workaround ?: if (intermediary != null)
            mappingResolver.mapClassName("intermediary", intermediary.replace("/", ".")) ?: name
        else name).replace(".", "/")
    }

    fun unmapClass(name: String): String {
        val intermediary = mappingResolver.unmapClassName("intermediary", name.replace("/", "."))
        return intermediarySrgMapping.remapClass(intermediary.replace(".", "/"))
    }

    val gameFile = getMCGameFile()
    lateinit var srgGamePath: Path

    private fun getDeobfJarDir(gameDir: Path, gameId: String, gameVersion: String): Path {
        return GameProviderHelper::class.java
            .getDeclaredMethod("getDeobfJarDir", Path::class.java, String::class.java, String::class.java)
            .apply {
                isAccessible = true
            }
            .invoke(null, gameDir, gameId, gameVersion) as Path
    }

    private fun getMCGameFile(): Path? {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment) {
            val commonJar = GameProviderHelper.getCommonGameJar()

            if (commonJar != null)
                return commonJar

            val sidedJar = GameProviderHelper.getEnvGameJar(FabricLoader.getInstance().environmentType)

            if (sidedJar != null)
                return sidedJar

            // this gives the obfuscated JAR, we don't want that
            //val inputGameJar = FabricLoader.getInstance().objectShare.get("fabric-loader:inputGameJar")
            //if (inputGameJar is Path)
            //return inputGameJar.toFile()

            // This is our best bet towards getting the Intermediary JAR.
            val deobfJar =
                getDeobfJarDir(
                    FabricLoader.getInstance().gameDir,
                    "minecraft",
                    KiltLoader.MC_VERSION.friendlyString
                ) / "${FabricLoader.getInstance().environmentType.name.lowercase()}-${launcher.targetNamespace}.jar"

            if (deobfJar.exists())
                return deobfJar
        } else {
            // TODO: is there a better way of doing this?
            val possibleMcGameJar = launcher.classPath.firstOrNull { path ->
                val str = path.absolutePathString()
                str.contains("net") && str.contains("minecraft") && str.contains("-loom.mappings.") && str.contains("minecraft-merged-")
            }

            return possibleMcGameJar
        }

        return null
    }

    suspend fun getGameClassPath(): Array<out Path> {
        return if (!FabricLoader.getInstance().isDevelopmentEnvironment)
            arrayOf(
                getMCGameFile()
                    ?: FabricLoader.getInstance().objectShare.get("fabric-loader:inputGameJar") as Path,
                Kilt::class.java.protectionDomain.codeSource.location.toURI().toPath()
            )
        else
            mutableListOf<Path>().apply {
                val remapClasspathFile = System.getProperty(SystemProperties.REMAP_CLASSPATH_FILE)
                    ?: throw RuntimeException("No remapClasspathFile provided")

                val content = withContext(Dispatchers.IO) { Path(remapClasspathFile).readText() }

                this.addAll(
                    content.split(File.pathSeparator.toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .map { Path(it) }
                )

                this.add(Kilt::class.java.protectionDomain.codeSource.location.toURI().toPath())
            }.toTypedArray()
    }

    private suspend fun remapMinecraft(): Path {
        val srgFile =
            KiltLoader.kiltCacheDir / "minecraft_${KiltLoader.MC_VERSION.friendlyString}-srg_$MC_MAPPED_JAR_VERSION.jar"

        if (srgFile.exists() && !forceRemap)
            return srgFile

        if (gameFile == null) {
            throw IllegalStateException("Minecraft JAR was not found!")
        }

        logger.info("Creating SRG-mapped Minecraft JAR for remapping Forge mods...")
        val startTime = System.currentTimeMillis()

        val classProvider = ClassProvider.builder().apply {
            this.addLibrary(gameFile)
            for (path in getGameClassPath()) {
                this.addLibrary(path)
            }
        }.build()
        val srgRemapper = EnhancedRemapper(classProvider, intermediarySrgMapping, logConsumer)

        val gameJar = withContext(Dispatchers.IO) { JarFile(gameFile.toFile()) }
        runCatching { srgFile.createFile() }

        withContext(Dispatchers.IO) { JarOutputStream(srgFile.outputStream()) }.use { outputJar ->
            gameJar.stream().consumeAsFlow().flowOn(Dispatchers.IO)
                .onEachAsync { entry ->
                    if (entry.name.endsWith(".class")) {
                        val classReader = ClassReader(gameJar.getInputStream(entry))

                        val classNode = ClassNode(Opcodes.ASM9)
                        classReader.accept(classNode, 0)

                        val classWriter = ClassWriter(0)

                        val visitor =
                            EnhancedClassRemapper(classWriter, srgRemapper, RenamingTransformer(srgRemapper, false))
                        classNode.accept(visitor)
                        ConflictingStaticMethodFixer.fixClass(classNode)

                        // We need to remap to the SRG name, otherwise the remapper completely fails in production environments.
                        val srgName =
                            intermediarySrgMapping.remapClass(entry.name.removePrefix("/").removeSuffix(".class"))

                        outputJar.putNextEntry(JarEntry("$srgName.class"))
                        outputJar.write(classWriter.toByteArray())
                        outputJar.closeEntry()
                    } else {
                        outputJar.putNextEntry(entry)
                        outputJar.write(gameJar.getInputStream(entry).readAllBytes())
                        outputJar.closeEntry()
                    }
                }
                .collect()
        }

        logger.info("Remapped Minecraft from Intermediary to SRG. (took ${System.currentTimeMillis() - startTime} ms)")

        return srgFile
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