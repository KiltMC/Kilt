package xyz.bluspring.kilt.loader

import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.toml.TomlParser
import com.google.gson.JsonParser
import cpw.mods.modlauncher.Launcher
import cpw.mods.modlauncher.api.IEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.stream.consumeAsFlow
import kotlinx.coroutines.withContext
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.ForgeStatesProvider
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.*
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ConfigTracker
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.*
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation
import net.minecraftforge.fml.loading.moddiscovery.ModClassVisitor
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo
import net.minecraftforge.fml.loading.moddiscovery.NightConfigWrapper
import net.minecraftforge.fml.loading.toposort.TopologicalSort
import net.minecraftforge.forgespi.Environment
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.MavenVersionAdapter
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.forgespi.locating.ModFileFactory
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.asm.AccessTransformerLoader
import xyz.bluspring.kilt.loader.asm.coremod.CoreModLoader
import xyz.bluspring.kilt.loader.mod.*
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.util.DistUtil
import xyz.bluspring.kilt.util.KiltHelper
import xyz.bluspring.kilt.util.buildGraph
import xyz.bluspring.knit.loader.KnitLoader
import xyz.bluspring.knit.loader.KnitModLoader
import xyz.bluspring.knit.loader.mod.ModDefinition
import xyz.bluspring.knit.loader.mod.ModDependency
import xyz.bluspring.knit.loader.mod.ModEnvironment
import xyz.bluspring.knit.loader.util.*
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile
import java.util.jar.Manifest
import kotlin.io.path.*

class KiltLoader : KnitModLoader<ForgeMod>(Kilt.MOD_ID, "Forge") {
    private val tomlParser = TomlParser()

    // I have no fucking clue why this is needed, but for whatever fucking reason,
    // the mods ObjectArrayList is getting resorted *after* it's getting sorted in scanMods,
    // no matter what the fuck I do.
    // I don't have time to deal with this, so this works instead.
    private lateinit var sortedModOrder: Collection<ForgeMod>

    private val environment = KiltEnvironment()

    // At this point, this is a wall of shame for mods that bundle both Forge and Fabric as one JAR, but don't actually
    // use the same mod ID.
    private val SKIPPED_FABRIC_MODS = mapOf(
        // Forge ID -> Fabric ID
        "unloaded_activity" to "unloadedactivity"
    )

    // This over here is a wall of shame for mods that use different mod IDs between their Forge and Fabric variants.
    private val FORGE_TO_FABRIC_MODS = mapOf(
        // Forge ID -> Fabric ID
        "cloth_config" to "cloth-config",
        "playeranimator" to "player-animator"
    )

    init {
        val loader = FabricLoader.getInstance()

        if (loader.environmentType == EnvType.CLIENT) {
            val KILT_ERROR_MESSAGE = "Kilt: Failed to start Kilt, please read the exception below!"

            // Kilt requires a hard dependency on Sodium, so let's just do this
            if (!loader.isModLoaded("sodium")) {
                KnitLoader.instance.displayError(KILT_ERROR_MESSAGE, IllegalStateException("Kilt: You are missing Sodium! Please install Sodium and Indium to ensure Kilt is capable of running as intended."))
            } else if (!loader.isModLoaded("indium")) {
                KnitLoader.instance.displayError(KILT_ERROR_MESSAGE, IllegalStateException("Kilt: You are missing Indium! Please install Indium to ensure Kilt is capable of running as intended."))
            } else if (loader.isModLoaded("embeddium")) {
                KnitLoader.instance.displayError(KILT_ERROR_MESSAGE, IllegalStateException("Kilt: You are using Embeddium, which is not supported under Kilt!"))
            }
        }
    }

    override fun getModDefinitions(path: Path): List<ModDefinition> {
        if (path.extension != "jar")
            return emptyList()

        // Load all mod definitions. This is recursive, and since we also need to handle JiJ, it's separated into another method.
        return loadModDefinitions(path)
    }

    override fun getNativeModId(dependencyId: String, nativeLoaderName: String): String {
        if (FORGE_TO_FABRIC_MODS.contains(dependencyId))
            return FORGE_TO_FABRIC_MODS[dependencyId]!!

        return super.getNativeModId(dependencyId, nativeLoaderName)
    }

    private fun loadModDefinitions(path: Path, parents: List<ModDefinition>? = null): List<ModDefinition> {
        val jarFile = JarFile(path.toFile())

        // Prevent users from having both Kilt and Connector at the same time.
        if (jarFile.getEntry("org/sinytra/connector/ConnectorUtil.class") != null) {
            throw Exception("Sinytra Connector was detected! I know I said \"Isn't it reasonable to have both?\", but come on!")
        }

        // Avoid loading JiJ'd MixinExtras, we already provide a modern version of it.
        if (jarFile.getEntry("com/llamalad7/mixinextras/injector/ModifyExpressionValue.class") != null) {
            return emptyList()
        }

        // Try to load manifest from JAR file, because it's required for some stuff in Forge mods.
        val manifest = try {
            jarFile.getInputStream(jarFile.getEntry("META-INF/MANIFEST.MF")).use { Manifest(it) }
        } catch (_: Throwable) {
            null
        }

        val definitions = mutableListOf<ModDefinition>()
        val modsTomlEntry = jarFile.getEntry("META-INF/mods.toml")

        // News flash! Apparently, Forge supports loading mods like this. I was not aware of that fact.
        if (manifest != null && parents == null && manifest.mainAttributes.getValue("FMLModType") != null)
            definitions.add(createCustomMod(path, manifest))
        else if (modsTomlEntry == null && parents == null)
            // If no mods.toml even exists, just skip it, unless it's JiJ'd.
            return emptyList()
        else if (modsTomlEntry != null) {
            // Load all mod definitions from the TOML.
            jarFile.getInputStream(modsTomlEntry)
                .use {
                    definitions.addAll(parseModsToml(path, tomlParser.parse(it, Charsets.UTF_8), manifest))
                }
        } else {
            // Load JiJ'd libraries
            definitions.add(createCustomMod(path, manifest))
        }

        val rootDefinitions = parents ?: definitions

        // Then, we also want to load all JiJ'd mods.
        val jarJarMetadata = jarFile.getEntry("META-INF/jarjar/metadata.json")

        if (jarJarMetadata != null) {
            val json = jarFile.getInputStream(jarJarMetadata).use { JsonParser.parseReader(it.reader(Charsets.UTF_8)).asJsonObject }
            val exception = RuntimeException("Failed to load JiJ data in mod ${path.fileName}!")

            // Iterate through the JARs that have been JiJ'd
            for (element in json.getAsJsonArray("jars")) {
                val data = element.asJsonObject
                val filePath = data.get("path").asString

                // If the entry doesn't actually exist, simply ignore.
                val entry = jarFile.getEntry(filePath) ?: continue
                val fileName = filePath.split("/").last()

                val file = jarFile.getInputStream(entry).use { extractedModsDir / "${HashUtils.md5Hash(it)}-$fileName" }

                runCatching {
                    file.createFile()
                    jarFile.getInputStream(entry).use { file.writeBytes(it.readAllBytes()) }
                }.onFailure { throwable ->
                    // Handle files that already exist, so we can just have it dealt with right off the bat.
                    if (throwable !is FileAlreadyExistsException && throwable !is java.nio.file.FileAlreadyExistsException && throwable is Exception) {
                        Kilt.logger.error("Failed to load JiJ'd file: $fileName", throwable)
                        exception.addSuppressed(throwable)
                    }
                }

                // Load through the definitions recursively until all JiJ'd mods have been loaded.
                try {
                    definitions.addAll(loadModDefinitions(file, rootDefinitions))
                } catch (e: Throwable) {
                    Kilt.logger.error("Failed to load JiJ'd file: $fileName", e)
                    exception.addSuppressed(e)
                }
            }

            // If something failed, make sure to throw the exception.
            if (exception.suppressed.isNotEmpty())
                throw exception
        }

        return definitions
    }

    // This is used specifically for JiJ'd mods that don't store mods.toml files.
    private fun createCustomMod(modFile: Path, manifest: Manifest?): ModDefinition {
        return ModDefinition(
            modFile,
            "jij_${modFile.nameWithoutExtension.lowercase().replace(Regex("[^a-zA-Z0-9_-]"), "")}",
            "(Kilt JiJ) ${modFile.nameWithoutExtension}",
            description = "This is a JIJ'd (Jar-in-Jar) mod that doesn't contain a mods.toml file, but has been loaded anyway.",
            version = ForgeModVersion(DefaultArtifactVersion("0.0.0")),
            license = "All Rights Reserved",

            additionalData = mapOf(
                "manifest" to manifest,
                "isJiJ" to true,
                "config" to NightConfigWrapper(tomlParser.parse(this::class.java.getResource("/default_mods.toml")))
            ),
            loaderCustomData = mapOf(
                "modmenu" to mapOf(
                    // Hide JiJ'd mods under the library badge.
                    "badges" to listOf("library")
                )
            )
        )
    }

    private fun parseModsToml(path: Path, toml: CommentedConfig, manifest: Manifest?, parentId: String? = null, isBuiltIn: Boolean = false): List<ModDefinition> {
        val fileName = path.fileName
        val modLoader = toml.get<String>("modLoader")

        // We need to check if the mod loader in the TOML is valid. Since we don't properly support ModLauncher or custom FML loading sequences, we need to implement support ourselves.
        if (modLoader != "javafml" && modLoader != "lowcodefml" && modLoader != "kotlinforforge") {
            throw IncompatibleModException("Forge mod file $fileName is not a supported FML mod! (got: $modLoader)")
        }

        val loaderVersionRange = MavenVersionAdapter.createFromVersionSpec(toml.get("loaderVersion"))
        when (modLoader) {
            "kotlinforforge" -> {
                if (!loaderVersionRange.containsVersion(Constants.KFF_VERSION)) {
                    throw IncompatibleModException("Forge mod file $fileName does not support Kotlin for Forge version ${Constants.KFF_VERSION}! (mod supports versions between [$loaderVersionRange])")
                }
            }

            "javafml", "lowcodefml" -> {
                if (!loaderVersionRange.containsVersion(SUPPORTED_FORGE_SPEC_VERSION)) {
                    throw IncompatibleModException("Forge mod file $fileName does not support Forge loader version ${SUPPORTED_FORGE_SPEC_VERSION}! (mod supports versions between [$loaderVersionRange])")
                }
            }
        }

        val definitions = mutableListOf<ModDefinition>()
        val mainConfig = NightConfigWrapper(toml)

        // Load all mod metadata in the TOML, since Forge allows mods to specify multiple mods in the TOML.
        for (metadata in mainConfig.getConfigList("mods")) {
            val modId = metadata.getConfigElement<String>("modId").orElseThrow {
                Exception("Forge mod file $fileName does not contain a mod ID!")
            }

            // ffs, why do we have to do this?
            // mods should really use the same mod ID between their mods >:(
            if (SKIPPED_FABRIC_MODS.contains(modId)) {
                Kilt.logger.warn("Mod ID $modId is a combined mod JAR already existing under ID ${SKIPPED_FABRIC_MODS[modId]}, skipping!")
                continue
            }

            val modVersion = ForgeModVersion(DefaultArtifactVersion(
                // Forge custom-replaces mod versions with string templates, so we need to handle that.
                metadata.getConfigElement<String>("version").orElse("1")
                    .run {
                        if (this == "\${file.jarVersion}")
                            manifest?.mainAttributes?.getValue("Implementation-Version") ?: this
                        else if (this == "\${global.forgeVersion}")
                            SUPPORTED_FORGE_API_VERSION.toString()
                        else if (this == "\${global.mcVersion}")
                            MC_VERSION.friendlyString
                        else this
                    }
            ))

            val dependencies = mutableListOf<ModDependency>()

            // Check all dependencies from the provided mod
            for (forgeDep in mainConfig.getConfigList("dependencies", modId)) {
                val versionRange = MavenVersionAdapter.createFromVersionSpec(
                    forgeDep.getConfigElement<String>("versionRange")
                        .orElseThrow { Exception("Forge mod file $fileName's dependencies contain a dependency without a version range!") }
                )

                dependencies.add(ModDependency(
                    id = forgeDep.getConfigElement<String>("modId").orElseThrow {
                        Exception("Forge mod file $fileName's dependencies contain a dependency without a mod ID!")
                    },
                    // Forge doesn't have nearly as much control over the dependency type, so handle required and optional only.
                    type = if (forgeDep.getConfigElement<Boolean>("mandatory").orElse(false))
                        ModDependency.Type.REQUIRED
                    else
                        ModDependency.Type.OPTIONAL,
                    constraint = ForgeVersionConstraint(versionRange),

                    // Forge has sided dependencies. How did we get sided dependencies before sided mods?
                    side = when (forgeDep.getConfigElement<String>("side").orElse("BOTH")) {
                        "CLIENT" -> ModEnvironment.CLIENT
                        "SERVER" -> ModEnvironment.SERVER
                        "BOTH" -> ModEnvironment.SERVER
                        else -> throw IllegalArgumentException("Invalid side ${forgeDep.getConfigElement<String>("side")} provided while handling Forge mod file $fileName!")
                    },

                    // Knit has no reason to handle ordering, but we do, so we store it into the additional data.
                    additionalData = mapOf(
                        "ordering" to IModInfo.Ordering.valueOf(forgeDep.getConfigElement<String>("ordering").orElse("NONE"))
                    )
                ))
            }

            val definition = ModDefinition(
                id = modId,
                displayName = metadata.getConfigElement<String>("displayName").orElse(modId),
                description = metadata.getConfigElement<String>("description").orElse("")
                    .replace("\r", ""), // Otherwise, the CR gets rendered weirdly into the newlines.
                authors = try {
                    metadata.getConfigElement<String>("authors").orElse("").split(",")
                } catch (_: ClassCastException) {
                    // this is apparently a possibility that I didn't know about? huh.
                    metadata.getConfigElement<List<String>>("authors").orElse(listOf())
                },
                version = modVersion,
                license = toml.get("license"),

                dependencies = dependencies,
                mixinConfigs = manifest?.mainAttributes?.getValue("MixinConfigs")?.split(",")
                    ?.map { ModDefinition.MixinConfig(it) }
                    ?: emptyList(),
                path = path,

                // Sets the parent ID of the mod definition
                parentId = parentId,

                icon = metadata.getConfigElement<String>("logoFile").orElse(""),

                // Forge mods handle both, there's no way to define sided mods.
                environment = ModEnvironment.BOTH,

                // If this mod is built-in, make sure to specify it.
                isBuiltin = isBuiltIn,

                additionalData = mapOf(
                    "manifest" to manifest,
                    "config" to mainConfig,
                    "loader" to modLoader
                ),

                loaderCustomData = mapOf(
                    // This is to trick ModMenu into giving Forge mods the "Forge" tag.
                    "patchwork:patcherMeta" to true
                )
            )

            definitions.add(definition)
        }

        return definitions
    }

    override fun getBuiltinModDefinitions(): List<ModDefinition> {
        return if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            val modsList = mutableListOf<ModDefinition>()

            for (url in this::class.java.classLoader.getResources("META-INF/forge.mods.toml")) {
                val toml = tomlParser.parse(url)
                modsList.addAll(parseModsToml(KiltLoader::class.java.protectionDomain.codeSource.location.toURI().toPath(), toml, null, isBuiltIn = true))
            }

            // Loads gametests
            for (url in this::class.java.classLoader.getResources("META-INF/mods.toml")) {
                val toml = tomlParser.parse(url)
                modsList.addAll(parseModsToml(KiltLoader::class.java.protectionDomain.codeSource.location.toURI().toPath(), toml, null, isBuiltIn = true))
            }

            modsList
        } else {
            val kiltFile = KiltLoader::class.java.protectionDomain.codeSource.location.toURI().toPath()
            val kiltJar = JarFile(kiltFile.toFile())

            val toml = tomlParser.parse(kiltJar.getInputStream(kiltJar.getJarEntry("META-INF/forge.mods.toml")))

            parseModsToml(kiltFile, toml, null, isBuiltIn = true)
        }
    }

    override fun finishModScanning() {
        val graph = this.mods.buildGraph()
        val sorted = TopologicalSort.topologicalSort(graph, null)

        // See comment at the lateinit
        sortedModOrder = sorted

        if (this.hasMod("embeddium")) {
            KnitLoader.instance.displayError("Kilt: You are using Embeddium, which is not supported under Kilt!", IllegalStateException())
        } else if (this.hasMod("rubidium")) {
            KnitLoader.instance.displayError("Kilt: You are using Rubidium, which is not supported under Kilt!", IllegalStateException())
        }

        // Scan all mod classes. This needs to be run early, because some Forge mods rely on scan data as early as mixin containers.
        scanModClasses()

        // Load mod access transformers and coremods
        for (mod in mods) {
            loadTransformers(mod)
            CoreModLoader.scanAndLoadCoreMods(mod)
        }
    }

    override suspend fun createModContainers(definitions: Collection<ModDefinition>): Collection<ForgeMod> {
        val remappedModsDir = (kiltCacheDir / "remappedMods").apply {
            runCatching { createDirectories() }
        }

        // Remaps all Forge mods from SRG to Intermediary/Yarn/MojMap
        try {
            KiltRemapper.remapMods(definitions, remappedModsDir)
        } catch (e: Throwable) {
            e.printStackTrace()
            throw RuntimeException("Errors occurred while remapping Forge mods!", e)
        }

        val mods = mutableListOf<ForgeMod>()

        // Then creates the mod containers for each mod.
        for (definition in definitions) {
            val config = definition.additionalData["config"] as NightConfigWrapper

            mods.add(ForgeMod(definition,
                showAsResourcePack = config.getConfigElement<Boolean>("showAsResourcePack").orElse(false),
                modConfig = config,
                modFile = definition.path.run {
                    if (this.extension == "jar")
                        this.toFile()
                    else null // If this is a built-in, let's not actually bother with it
                },
                // Some JiJ'd mods don't have TOML files, but we need to check if they have the "GAMELIBRARY" attribute,
                // because then that verifies that we need to scan it.
                shouldScan = definition.additionalData["isJiJ"] != true || (definition.additionalData["manifest"] as? Manifest?)?.mainAttributes?.getValue("FMLModType") == "GAMELIBRARY"
            ))
        }

        return mods
    }

    override fun preInitialize() {
        // DON'T TRY TO MAKE THIS USE "Environment.Keys".
        // OTHERWISE THE BUILD WILL FAIL.
        environment.computePropertyIfAbsent(IEnvironment.buildKey("FORGEDIST", Dist::class.java).get()) { DistUtil.envTypeToDist(FabricLoader.getInstance().environmentType) }
        environment.computePropertyIfAbsent(IEnvironment.buildKey("MODFILEFACTORY", ModFileFactory::class.java).get()) { KiltModFileFactory() }

        environment.computePropertyIfAbsent(IEnvironment.Keys.VERSION.get()) { MC_VERSION.friendlyString }
        Launcher.INSTANCE.environment().computePropertyIfAbsent(IEnvironment.Keys.VERSION.get()) { MC_VERSION.friendlyString }
        environment.computePropertyIfAbsent(IEnvironment.Keys.GAMEDIR.get()) { FabricLoader.getInstance().gameDir }
        environment.computePropertyIfAbsent(IEnvironment.Keys.ASSETSDIR.get()) { Path(FabricLoaderImpl.INSTANCE.gameProvider.arguments.getOrDefault("assetsDir", FabricLoader.getInstance().gameDir.absolutePathString())) }
        environment.computePropertyIfAbsent(IEnvironment.Keys.LAUNCHTARGET.get()) { FabricLoader.getInstance().environmentType.name.lowercase() }
        environment.computePropertyIfAbsent(IEnvironment.Keys.UUID.get()) { FabricLoaderImpl.INSTANCE.gameProvider.arguments.getOrDefault("uuid", "00000000-00000000-00000000-00000000") }
        Environment.build(environment) // Use Kilt's environment

        // Load all of the Forge access transformers
        AccessTransformerLoader.runTransformers()
    }

    fun scanModClasses() {
        Kilt.logger.info("Scanning all Forge mod classes...")

        val exception = RuntimeException("Failed to scan Forge mod classes in Kilt!")

        runBlocking {
            launch(Dispatchers.Default) {
                // Load Forge scan data immediately, then we can assign it when we need to.
                val forgeScanData = ModFileScanData()

                KiltHelper.getForgeClassNodes().asFlow().collect {
                    val visitor = ModClassVisitor()
                    it.accept(visitor)

                    visitor.buildData(forgeScanData.classes, forgeScanData.annotations)
                }

                // TODO: Need to make sure to group mods together so they load in the correct order from each other
                sortedModOrder.asFlow().concurrent()
                    .collect { mod ->
                        if (!mod.shouldScan) {
                            mod.scanData = ModFileScanData()
                            return@collect
                        }

                        if (mod.modFile == null) { // This is usually a Forge built-in, we don't have to worry about scanning this.
                            // If it is in fact a built-in, let's assign the scan data.
                            if (mod.definition.isBuiltin) {
                                forgeScanData.addModFileInfo(ModFileInfo(mod))
                                mod.scanData = forgeScanData
                            }

                            return@collect
                        }

                        val scanData = ModFileScanData()
                        scanData.addModFileInfo(ModFileInfo(mod))

                        mod.scanData = scanData

                        val classes = ConcurrentHashMap.newKeySet<ModFileScanData.ClassData>()
                        val annotations = ConcurrentHashMap.newKeySet<ModFileScanData.AnnotationData>()

                        // basically emulate how Forge loads stuff
                        mod.jar.stream().consumeAsFlow().concurrent()
                            .filter { it.name.endsWith(".class") }
                            .collect { entry ->
                                val visitor = ModClassVisitor()
                                val classReader = withContext(Dispatchers.IO) { mod.jar.getInputStream(entry) }.use { ClassReader(it) }

                                classReader.accept(visitor, 0)
                                visitor.buildData(classes, annotations)
                            }

                        // This needs to be sorted, otherwise there is a very high possibility of packet desync between client-server,
                        // because for whatever reason Forge uses int packet IDs and MCreator mods don't register packets in one place.
                        scanData.classes.addAll(classes.sortedWith { a, b -> a.clazz.className.compareTo(b.clazz.className) })
                        scanData.annotations.addAll(annotations.sortedWith { a, b -> a.clazz.className.compareTo(b.clazz.className) })
                    }
            }.join()
        }

        if (exception.suppressed.isNotEmpty()) {
            exception.printStackTrace()
            KnitLoader.instance.displayError("Errors occurred while scanning Forge mod classes!", exception)
        }
    }

    fun loadMods() {
        Kilt.logger.info("Starting initialization of Forge mods...")

        val exception = RuntimeException("Failed to load Forge mods in Kilt!")

        // Initialize @Mod annotated constructors
        initMods(exception)

        // Register @EventBusSubscriber annotations
        runBlocking {
            launch(Dispatchers.Default) {
                // TODO: Need to make sure to group mods together so they load in the correct order from each other
                sortedModOrder.asFlow().concurrent()
                    .collect { mod ->
                        try {
                            registerAnnotations(mod, mod.scanData)
                        } catch (e: Throwable) {
                            e.printStackTrace()
                            exception.addSuppressed(e)
                        }
                    }
            }.join()
        }

        // Then construct mods in the CONSTRUCT loading stage
        constructMods(exception)

        if (exception.suppressed.isNotEmpty()) {
            exception.printStackTrace()
            KnitLoader.instance.displayError("Errors occurred while loading Forge mods!", exception)
        }
    }

    private val launcher = FabricLauncherBase.getLauncher()

    private suspend fun registerAnnotations(mod: ForgeMod, scanData: ModFileScanData) {
        val exception = RuntimeException("Failed to register annotations for mod ${mod.displayName} (${mod.modId})!")

        // Automatically subscribe events
        scanData.annotations.asFlow()
            .filter { it.annotationType == AUTO_SUBSCRIBE_ANNOTATION }
            .collect { annotation ->
                // it.annotationData["modid"] as String
                // it.annotationData["bus"] as Mod.EventBusSubscriber.Bus

                try {
                    val modId = annotation.annotationData["modid"] as String?
                        // Use the mod ID of the mod in the class instead
                        ?: scanData.annotations.firstOrNull { a -> checkTypeOrParentsAreType(a.clazz, annotation.clazz) && a.annotationType == MOD_ANNOTATION }?.annotationData?.get("value") as? String?
                        ?: mod.modId

                    if (modId != mod.modId)
                        return@collect

                    val busType = Mod.EventBusSubscriber.Bus.valueOf(
                        if (annotation.annotationData.contains("bus"))
                            (annotation.annotationData["bus"] as ModAnnotation.EnumHolder).value!!
                        else "FORGE"
                    )

                    val dists = if (annotation.annotationData.contains("value"))
                        (annotation.annotationData["value"] as List<ModAnnotation.EnumHolder>).map { Dist.valueOf(it.value!!) }
                    else
                        listOf()

                    if (dists.isNotEmpty() && dists.none { DistUtil.distToEnvType(it) == FabricLoader.getInstance().environmentType }) {
                        return@collect
                    }

                    ModLoadingContext.kiltActiveModId = modId

                    val clazz = Class.forName(annotation.clazz.className, true, this::class.java.classLoader)
                    val obj = try { clazz.kotlin.objectInstance } catch (_: Throwable) { null }

                    if (obj != null)
                        busType.bus().get().register(obj)
                    else
                        busType.bus().get().register(clazz)

                    ModLoadingContext.kiltActiveModId = null

                    Kilt.logger.debug("Automatically registered event ${annotation.clazz.className} from mod ID $modId under bus ${busType.name}")
                } catch (e: Throwable) {
                    Kilt.logger.error("Failed to register event ${annotation.clazz.className} from mod ${mod.modId}!")
                    val ex = RuntimeException("Failed to register event ${annotation.clazz.className} from mod ${mod.modId}!", e)
                    ex.printStackTrace()
                    exception.addSuppressed(ex)
                }
            }

        if (exception.suppressed.isNotEmpty())
            throw exception
    }

    private fun checkTypeOrParentsAreType(rootType: Type, topType: Type): Boolean {
        if (topType == rootType)
            return true

        if (!topType.className.contains("$"))
            return false

        val classNameSplit = topType.className.split("$")
        for ((index, typeLvl) in classNameSplit.withIndex()) {
            if (index == 0 && Type.getType(Class.forName(typeLvl, false, FabricLauncherBase.getLauncher().targetClassLoader)) == rootType)
                return true
            else {
                val combined = classNameSplit.chunked(index + 1)[0].joinToString("$")
                if (Type.getType(Class.forName(combined, false, FabricLauncherBase.getLauncher().targetClassLoader)) == rootType)
                    return true
            }
        }

        return false
    }

    private fun initMods(exception: Exception) {
        runBlocking {
            sortedModOrder.asFlow()
                .collect { mod ->
                    try {
                        initMod(mod, mod.scanData)
                    } catch (e: Throwable) {
                        Kilt.logger.error("Failed to load mod ${mod.displayName} (${mod.modId})!")
                        e.printStackTrace()
                        exception.addSuppressed(RuntimeException("Failed to load mod ${mod.displayName} (${mod.modId})", e))
                    }
                }
        }
    }

    private fun constructMods(exception: Exception) {
        try {
            ModLoadingStage.CONSTRUCT.deferredWorkQueue.runTasks()
        } catch (e: Throwable) {
            e.printStackTrace()
            exception.addSuppressed(e)
        }
    }

    suspend fun initMod(mod: ForgeMod, scanData: ModFileScanData) {
        val exception = RuntimeException("Failed to load mod ${mod.displayName} (${mod.modId})!")

        // Datapack mod, don't try to init
        if (mod.loader == "lowcodefml")
            return

        // this should probably belong to FMLJavaModLanguageProvider, but I doubt there's any mods that use it.
        // I hope.
        var hasInitialized = false
        var hasErrored = false
        scanData.annotations.asFlow()
            .filter { it.annotationType == MOD_ANNOTATION }
            .collect {
                // it.clazz.className - Class
                // it.annotationData["value"] as String - Mod ID

                var extraThrowable: Throwable? = null

                try {
                    val modId = it.annotationData["value"] as String

                    if (modId != mod.modId)
                        return@collect

                    ModLoadingContext.kiltActiveModId = modId

                    val clazz = launcher.loadIntoTarget(it.clazz.className)
                    val ktObj = try { clazz.kotlin.objectInstance } catch (e: Throwable) {
                        extraThrowable = e
                        null
                    }

                    if (ktObj != null) {
                        // Load mods created using KFF
                        mod.modObject = ktObj
                    } else {
                        // Otherwise, initialize under the regular Java process
                        try {
                            val constructor = clazz.getDeclaredConstructor(FMLJavaModLoadingContext::class.java)
                            val ctx = FMLJavaModLoadingContext.kiltGetContext(mod)

                            mod.modObject = constructor.newInstance(ctx)
                        } catch (_: NoSuchMethodException) {
                            mod.modObject = clazz.getDeclaredConstructor().newInstance()
                        }
                    }

                    Kilt.logger.info("Initialized new instance of mod $modId.")
                    hasInitialized = true

                    ModLoadingContext.kiltActiveModId = null
                } catch (e: Throwable) {
                    e.printStackTrace()
                    exception.addSuppressed(extraThrowable)
                    exception.addSuppressed(e)
                    hasErrored = true
                }
            }

        if (!hasInitialized && mod.shouldScan && !mod.modId.startsWith("jij_") && !hasErrored) {
            exception.addSuppressed(IllegalStateException("Mod ID ${mod.modId} is an invalid Java FML mod!"))
        }

        if (exception.suppressed.isNotEmpty()) {
            throw exception
        }

        ModLoadingContext.kiltActiveModId = mod.modId
        mod.eventBus.post(FMLConstructModEvent(mod.container, ModLoadingStage.CONSTRUCT))
        ModLoadingContext.kiltActiveModId = null
    }

    private fun loadTransformers(mod: ForgeMod) {
        if (mod.modFile == null || mod.definition.isBuiltin) {
            val accessTransformer = KiltLoader::class.java.getResource("META-INF/accesstransformer.cfg")

            if (accessTransformer != null) {
                Kilt.logger.info("Found access transformer for Forge")
                AccessTransformerLoader.convertTransformers(accessTransformer.readBytes())
            }

            return
        }

        try {
            val accessTransformer = mod.jar.getEntry("META-INF/accesstransformer.cfg")

            if (accessTransformer != null) {
                Kilt.logger.info("Found access transformer for ${mod.modId}")
                AccessTransformerLoader.convertTransformers(mod.jar.getInputStream(accessTransformer).readAllBytes())
            }
        } catch (e: UninitializedPropertyAccessException) { // Forge special case
            val accessTransformer = KiltLoader::class.java.getResource("META-INF/accesstransformer.cfg")

            if (accessTransformer != null) {
                Kilt.logger.info("Found access transformer for ${mod.modId}")
                AccessTransformerLoader.convertTransformers(accessTransformer.readBytes())
            }
        }
    }

    fun postEvent(ev: Event) {
        mods.forEach {
            it.eventBus.post(ev)
        }
    }

    fun getMod(id: String): ForgeMod? {
        return mods.firstOrNull { it != null && it.modId == id }
    }

    fun hasMod(id: String): Boolean {
        return mods.any { it != null && it.modId == id }
    }

    private var statesProvider: ForgeStatesProvider? = null

    private val fmlPhases = mutableMapOf(
        ModLoadingPhase.LOAD to {
            // CONFIG_LOAD
            if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
                ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, FMLPaths.CONFIGDIR.get());
            } else {
                ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.SERVER, FMLPaths.CONFIGDIR.get());
            }
            ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.COMMON, FMLPaths.CONFIGDIR.get());

            // COMMON_SETUP
            ModLoader.get()
                .kiltPostEventWrappingModsBuildEvent { FMLCommonSetupEvent(it.container, ModLoadingStage.COMMON_SETUP) }

            ModLoadingStage.COMMON_SETUP.deferredWorkQueue.runTasks()

            // SIDED_SETUP
            ModLoader.get().kiltPostEventWrappingModsBuildEvent {
                if (FabricLoader.getInstance().environmentType == EnvType.CLIENT)
                    FMLClientSetupEvent(it.container, ModLoadingStage.SIDED_SETUP)
                else
                    FMLDedicatedServerSetupEvent(it.container, ModLoadingStage.SIDED_SETUP)
            }

            ModLoadingStage.SIDED_SETUP.deferredWorkQueue.runTasks()

            // ENQUEUE_IMC
            ModLoader.get()
                .kiltPostEventWrappingModsBuildEvent { InterModEnqueueEvent(it.container, ModLoadingStage.ENQUEUE_IMC) }

            ModLoadingStage.ENQUEUE_IMC.deferredWorkQueue.runTasks()

            // PROCESS_IMC
            ModLoader.get()
                .kiltPostEventWrappingModsBuildEvent { InterModProcessEvent(it.container, ModLoadingStage.PROCESS_IMC) }

            ModLoadingStage.PROCESS_IMC.deferredWorkQueue.runTasks()

            // COMPLETE
            ModLoader.get().kiltPostEventWrappingModsBuildEvent { FMLLoadCompleteEvent(it.container, ModLoadingStage.COMPLETE) }

            ModLoadingStage.COMPLETE.deferredWorkQueue.runTasks()
        }
    )

    fun runPhaseExecutors(phase: ModLoadingPhase) {
        if (statesProvider == null)
            statesProvider = ForgeStatesProvider()

        val sortedStates = statesProvider!!.allStates.filter { it.phase() == phase }.sortedWith { first, second ->
            if (first.previous() == second.name())
                1
            else if (first.name() == second.previous())
                0
            else
                -1
        }

        fmlPhases[phase]?.invoke()

        for (state in sortedStates) {
            println("running ${state.name()} in ${state.phase()}")

            state.inlineRunnable().ifPresent { consumer ->
                consumer.accept(ModList.get())
            }
        }
    }

    companion object {
        val instance: KiltLoader
            get() = KnitLoader.instance.getLoaderById("kilt") as KiltLoader

        // These constants are to be updated each time we change versions
        val SUPPORTED_FORGE_SPEC_VERSION = Constants.FORGE_LOADER_VERSION
        val SUPPORTED_FORGE_API_VERSION = Constants.FORGE_API_VERSION
        val MC_VERSION = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().metadata.version

        private val MOD_ANNOTATION = Type.getType(Mod::class.java)
        private val AUTO_SUBSCRIBE_ANNOTATION = Type.getType(Mod.EventBusSubscriber::class.java)

        val kiltCacheDir = (FabricLoader.getInstance().gameDir / ".kilt").apply {
            runCatching { this.createDirectories() }
        }
        private val extractedModsDir = (kiltCacheDir / "extractedMods").apply {
            runCatching { this.createDirectories() }
        }
    }
}