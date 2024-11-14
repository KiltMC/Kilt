package xyz.bluspring.kilt.loader

import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.toml.TomlParser
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.loader.impl.gui.FabricGuiEntry
import net.fabricmc.loader.impl.gui.FabricStatusTree
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraftforge.common.ForgeStatesProvider
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.*
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ConfigTracker
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.*
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation
import net.minecraftforge.fml.loading.moddiscovery.ModClassVisitor
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo
import net.minecraftforge.fml.loading.moddiscovery.NightConfigWrapper
import net.minecraftforge.fml.loading.toposort.TopologicalSort
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModInfo.DependencySide
import net.minecraftforge.forgespi.language.MavenVersionAdapter
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.registries.ForgeRegistries
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.asm.AccessTransformerLoader
import xyz.bluspring.kilt.loader.asm.CoreModLoader
import xyz.bluspring.kilt.loader.mod.ForgeMod
import xyz.bluspring.kilt.loader.mod.LoaderModProvider
import xyz.bluspring.kilt.loader.mod.fabric.FabricModProvider
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.util.DeltaTimeProfiler
import xyz.bluspring.kilt.util.KiltHelper
import xyz.bluspring.kilt.util.buildGraph
import java.net.URL
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer
import java.util.jar.JarFile
import java.util.jar.Manifest
import java.util.zip.ZipFile
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.toPath
import kotlin.io.path.writeBytes
import kotlin.system.exitProcess

class KiltLoader {
    val mods = mutableListOf<ForgeMod>()
    internal val modLoadingQueue = ConcurrentLinkedQueue<ForgeMod>()
    private val tomlParser = TomlParser()

    // Meant to be used for compatibility between Fabric and other derivatives of it, such as Quilt.
    // However, I currently haven't found a way to link Kilt's mods into Quilt, so this is how it will
    // be for now.
    val modProvider: LoaderModProvider = FabricModProvider()

    val scope = CoroutineScope(SupervisorJob())

    val scanModJob by lazy {
        scope.launch(Dispatchers.IO) { scanMods() }
    }

    suspend fun scanMods() {
        Kilt.logger.info("Scanning the mods directory for Forge mods...")
        DeltaTimeProfiler.push("scanMods")

        val modsDir = FabricLoader.getInstance().gameDir / "mods"

        if (!modsDir.exists() || !modsDir.isDirectory())
            throw IllegalStateException("Mods directory doesn't exist! ...how did you even get to this point?")


        val thrownExceptions = mutableMapOf<String, Exception>()

        DeltaTimeProfiler.push("preload")
        modsDir.forEachDirectoryEntry("*.jar") { modFile ->
            thrownExceptions.putAll(preloadJarMod(modFile, ZipFile(modFile.toFile())))
        }
        DeltaTimeProfiler.pop()

        // If exceptions had occurred during preloading, then create a window to show the exceptions.
        if (thrownExceptions.isNotEmpty()) {
            Kilt.logger.error("Failed to load Forge mods in Kilt!")

            for ((name, exception) in thrownExceptions) {
                Kilt.logger.error("- $name failed to load! Exception:")
                Kilt.logger.error(exception.stackTraceToString())
            }

            FabricGuiEntry.displayError("Exceptions occurred whilst loading Forge mods in Kilt!", null, {
                val errorTab = it.addTab("Kilt Error")

                thrownExceptions.forEach { (name, exception) ->
                    errorTab.node.addMessage("$name failed to load!", FabricStatusTree.FabricTreeWarningLevel.ERROR)
                        .addCleanedException(exception)
                }

                // Little workaround to show the custom tab
                it.tabs.removeIf { tab -> tab != errorTab }
            }, true)

            exitProcess(1)
        }

        Kilt.logger.debug("Re-scanning Forge mods to verify mod dependencies...")

        val mcVersion = DefaultArtifactVersion(
            FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().metadata.version.friendlyString
        )
        val preloadedMods = mutableMapOf<ForgeMod, List<ModLoadingState>>()

        // Iterate through the mod loading queue for the first time
        // to validate dependencies.
        modLoadingQueue.forEach { mod ->
            val dependencies = mutableListOf<ModLoadingState>()
            mod.dependencies.forEach dependencies@{ dependency ->
                if (!isSideValid(dependency.side))
                    return@dependencies // Don't need to load the dependency.

                if (dependency.modId == "forge") {
                    if (!dependency.versionRange.containsVersion(SUPPORTED_FORGE_API_VERSION)) {
                        dependencies.add(
                            IncompatibleDependencyLoadingState(
                                dependency,
                                SUPPORTED_FORGE_API_VERSION
                            )
                        )

                        return@dependencies
                    }

                    dependencies.add(ValidDependencyLoadingState(dependency))

                    return@dependencies
                } else if (dependency.modId == "minecraft") {
                    if (!dependency.versionRange.containsVersion(mcVersion)) {
                        dependencies.add(
                            IncompatibleDependencyLoadingState(
                                dependency,
                                mcVersion
                            )
                        )

                        return@dependencies
                    }

                    dependencies.add(ValidDependencyLoadingState(dependency))

                    return@dependencies
                }

                if ( // Check if the dependency exists, and if it's required.
                    modLoadingQueue.none { it.modId == dependency.modId } &&
                    !FabricLoader.getInstance().isModLoaded(dependency.modId) &&
                    dependency.isMandatory
                ) {
                    dependencies.add(MissingDependencyLoadingState(dependency))
                    return@dependencies
                }

                // If it's not required, no need to worry.
                if (modLoadingQueue.none { it.modId == dependency.modId } && !FabricLoader.getInstance()
                        .isModLoaded(dependency.modId))
                    return@dependencies

                val dependencyMod = modLoadingQueue.firstOrNull { it.modId == dependency.modId }

                if (dependencyMod == null && FabricLoader.getInstance().isModLoaded(dependency.modId)) {
                    val dependencyContainer = FabricLoader.getInstance().getModContainer(dependency.modId).orElseThrow()
                    val version = DefaultArtifactVersion(dependencyContainer.metadata.version.friendlyString)

                    if (dependency.versionRange.containsVersion(version)) {
                        dependencies.add(ValidDependencyLoadingState(dependency))
                    } else {
                        dependencies.add(IncompatibleDependencyLoadingState(dependency, version))
                    }

                    return@dependencies
                } else if (dependencyMod == null) {
                    dependencies.add(MissingDependencyLoadingState(dependency))
                    return@dependencies
                }

                if (!dependency.versionRange.containsVersion(dependencyMod.version)) {
                    dependencies.add(
                        IncompatibleDependencyLoadingState(
                            dependency,
                            dependencyMod.version
                        )
                    )

                    return@dependencies
                }

                dependencies.add(ValidDependencyLoadingState(dependency))
            }

            preloadedMods[mod] = dependencies
        }

        // Check if any of the dependencies failed to load
        if (preloadedMods.any { it.value.any { state -> state !is ValidDependencyLoadingState } }) {
            preloadedMods.filter { mod -> mod.value.any { state -> state !is ValidDependencyLoadingState } }
                .forEach { (mod, dependencyStates) ->
                    Kilt.logger.error("${mod.displayName} (${mod.modId}) failed to load!")

                    dependencyStates.forEach states@{ state ->
                        if (state is ValidDependencyLoadingState)
                            return@states

                        Kilt.logger.error("- Dependency ${state.dependency.modId} failed to load: $state")
                    }
                }

            FabricGuiEntry.displayError("Incompatible Forge mod set!", null, {
                val tab = it.addTab("Kilt Error")

                preloadedMods.filter { mod -> mod.value.any { state -> state !is ValidDependencyLoadingState } }
                    .forEach { (mod, dependencyStates) ->
                        val message = tab.node.addMessage(
                            "${mod.displayName} (${mod.modId}) failed to load!",
                            FabricStatusTree.FabricTreeWarningLevel.ERROR
                        )

                        dependencyStates.forEach states@{ state ->
                            if (state is ValidDependencyLoadingState)
                                return@states

                            message.addMessage(
                                "Dependency ${state.dependency.modId} failed to load: $state",
                                FabricStatusTree.FabricTreeWarningLevel.NONE
                            )
                        }
                    }

                it.tabs.removeIf { t -> t != tab }
            }, true)

            exitProcess(1)
        } else {
            Kilt.logger.info("Found ${preloadedMods.size} Forge mods.")

            if (preloadedMods.isNotEmpty()) {
                try {
                    remapMods()
                } catch (e: Exception) {
                    e.printStackTrace()

                    FabricGuiEntry.displayError("Failed to remap Forge mods!", e, {
                        val tab = it.addTab("Kilt Error")

                        it.tabs.removeIf { t -> t != tab }
                    }, true)

                    exitProcess(1)
                }
            } else {
                Kilt.logger.info("No Forge mods located, not proceeding with mod remapping.")
            }

            modLoadingQueue.forEach { mod ->
                loadTransformers(mod)
                CoreModLoader.scanAndLoadCoreMods(mod)
            }

            loadTransformers(null) // load Forge ATs
        }

        sortMods(modLoadingQueue)

        DeltaTimeProfiler.push("addToClassPath")
        for (mod in modLoadingQueue) {
            Kilt.loader.addModToFabric(mod)
            FabricLauncherBase.getLauncher().addToClassPath(mod.remappedModFile.toURI().toPath())
        }
        DeltaTimeProfiler.pop()

        DeltaTimeProfiler.pop()
    }

    private fun sortMods(queue: ConcurrentLinkedQueue<ForgeMod>) {
        DeltaTimeProfiler.push("sortMods")
        val graph = queue.buildGraph()
        val sorted = TopologicalSort.topologicalSort(graph, null)
        queue.clear()
        queue.addAll(sorted)
        DeltaTimeProfiler.pop()
    }

    fun preloadMods() {
        loadForgeBuiltinMod() // fuck you
    }

    // Apparently, Forge has itself as a mod. But Kilt will refuse to handle itself, as it's a Fabric mod.
    // Let's do a trick to load the Forge built-in mod.
    private fun loadForgeBuiltinMod() {
        val forgeMod = if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            val toml = tomlParser.parse(this::class.java.getResource("/META-INF/forge.mods.toml"))
            parseModsToml(toml, null, null).first()
        } else {
            val kiltFile = KiltLoader::class.java.protectionDomain.codeSource.location.toURI().toPath()
            val kiltJar = JarFile(kiltFile.toFile())

            val toml = tomlParser.parse(kiltJar.getInputStream(kiltJar.getJarEntry("META-INF/forge.mods.toml")))

            parseModsToml(toml, kiltFile, kiltJar).first()
        }

        val scanData = ModFileScanData()
        scanData.addModFileInfo(ModFileInfo(forgeMod))

        forgeMod.scanData = scanData

        KiltHelper.getForgeClassNodes().forEach {
            val visitor = ModClassVisitor()
            it.accept(visitor)

            visitor.buildData(scanData.classes, scanData.annotations)
        }

        CoreModLoader.scanAndLoadCoreMods(forgeMod)

        mods.add(forgeMod)
        addModToFabric(forgeMod)
    }

    private fun fullLoadForgeBuiltin() {
        val mod = this.getMod("forge") ?: throw IllegalStateException("WHAT")

        registerAnnotations(mod, mod.scanData)
        mod.eventBus.post(FMLConstructModEvent(mod, ModLoadingStage.CONSTRUCT))
    }

    // This is used specifically for JiJ'd mods that don't store mods.toml files.
    private fun createCustomMod(modFile: Path): ForgeMod {
        return ForgeMod(
            "jij_${modFile.nameWithoutExtension.lowercase().replace(Regex("[^a-zA-Z0-9_-]"), "")}",
            "(Kilt JiJ) ${modFile.nameWithoutExtension}",
            description = "This is a JIJ'd (Jar-in-Jar) mod that doesn't contain a mods.toml file, but has been loaded anyway.",
            DefaultArtifactVersion("0.0.0"),
            modFile = modFile.toFile(),
            modConfig = NightConfigWrapper(tomlParser.parse(this::class.java.getResource("/default_mods.toml")))
        )
    }

    private fun preloadJarMod(
        modFile: Path,
        jarFile: ZipFile,
        nestedModUpdater: Consumer<ForgeMod>? = null
    ): Map<String, Exception> {
        // Do NOT load Fabric mods.
        // Some mod JARs actually store both Forge and Fabric in one JAR by using Forgix.
        // Since Fabric loads the Fabric mod before we can even get to it, we shouldn't load the Forge variant
        // ourselves to avoid mod conflicts. And because Kilt is still in an unstable state.
        if (
            jarFile.getEntry("fabric.mod.json") != null
        )
            return mapOf()

        val thrownExceptions = mutableMapOf<String, Exception>()
        DeltaTimeProfiler.push(modFile.nameWithoutExtension)

        Kilt.logger.debug("Scanning jar file ${modFile.name} for Forge mod metadata.")

        try {
            val modsToml = jarFile.getEntry("META-INF/mods.toml")

            if (nestedModUpdater != null && modsToml == null) {
                val mod = createCustomMod(modFile)

                if (FabricLoader.getInstance()
                        .isModLoaded(mod.modId) || FabricLoaderImpl.INSTANCE.getModCandidate(mod.modId) != null
                ) {
                    Kilt.logger.warn("Duplicate Forge and Fabric mod IDs detected: ${mod.modId}")
                    return mapOf()
                }

                // Avoid loading mods twice
                if (modLoadingQueue.any { it.modId == mod.modId })
                    return mapOf()

                modLoadingQueue.add(mod)

                Kilt.logger.info("Loaded JiJ'd mod ${modFile.nameWithoutExtension}.")
                nestedModUpdater.accept(mod)

                DeltaTimeProfiler.pop()
                return mapOf()
            }

            // Check for Forge's method of include.
            // Doing it this way is probably faster than scanning the entire JAR.
            val jarJarMetadata = jarFile.getEntry("META-INF/jarjar/metadata.json")

            val nestedMods = mutableListOf<ForgeMod>()

            if (jarJarMetadata != null) {
                val json = JsonParser.parseReader(jarFile.getInputStream(jarJarMetadata).reader()).asJsonObject

                json.getAsJsonArray("jars").forEach {
                    val data = it.asJsonObject
                    val filePath = data.get("path").asString

                    val entry = jarFile.getEntry(filePath) ?: return@forEach

                    // Use the CRC as a way of having a unique point of storage, so
                    // if the file already exists, no need to extract it again.
                    val fileName = filePath.split("/").last()

                    val file = extractedModsDir / "${entry.crc}-$fileName"
                    runCatching {
                        file.createFile()
                        file.writeBytes(jarFile.getInputStream(entry).readAllBytes())
                    }.onFailure { throwable ->
                        if (throwable !is FileAlreadyExistsException && throwable is Exception) {
                            Kilt.logger.error("Failed to load JiJ'd file: $fileName", throwable)
                            thrownExceptions[fileName] = throwable
                            return@forEach
                        }
                    }

                    preloadJarMod(file, ZipFile(file.toFile())) { mod ->
                        nestedMods.add(mod)
                    }
                }
            }

            val toml = tomlParser.parse(jarFile.getInputStream(modsToml))
            val forgeMods = parseModsToml(toml, modFile, jarFile, nestedMods)

            forgeMods.forEach {
                modLoadingQueue.add(it)
                Kilt.logger.info("Discovered Forge mod ${it.displayName} (${it.modId}) version ${it.version} (${modFile.name})")
            }
        } catch (e: Exception) {
            thrownExceptions[modFile.name] = e
            e.printStackTrace()
        }

        DeltaTimeProfiler.pop()
        return thrownExceptions
    }

    // Split this off from the main preloadMods method, in case it needs to be used again later.
    private fun parseModsToml(
        toml: CommentedConfig,
        modFile: Path?,
        jarFile: ZipFile?,
        nestedMods: List<ForgeMod> = listOf()
    ): List<ForgeMod> {
        if (toml.get("modLoader") as String != "javafml" && toml.get("modLoader") as String != "lowcodefml")
            throw Exception(
                "Forge mod file ${modFile?.name ?: "(unknown)"} is not a supported FML mod! (got ${
                    toml.get(
                        "modLoader"
                    ) as String
                })"
            )

        // Load the JAR's manifest file, or at least try to.
        val manifest = if (jarFile != null) try {
            Manifest(jarFile.getInputStream(jarFile.getEntry("META-INF/MANIFEST.MF")))
        } catch (_: Exception) {
            null
        } else null

        val fileName = modFile?.name ?: "(unknown)"

        val loaderVersionRange = MavenVersionAdapter.createFromVersionSpec(toml.get("loaderVersion") as String)
        if (!loaderVersionRange.containsVersion(SUPPORTED_FORGE_SPEC_VERSION))
            throw Exception("Forge mod file $fileName does not support Forge loader version $SUPPORTED_FORGE_SPEC_VERSION (mod supports versions between [$loaderVersionRange]))")

        val mainConfig = NightConfigWrapper(toml)

        val modsMetadataList = mainConfig.getConfigList("mods")
        val forgeMods = mutableListOf<ForgeMod>()

        modsMetadataList.forEach { metadata ->
            val modId = metadata.getConfigElement<String>("modId").orElseThrow {
                Exception("Forge mod file $fileName does not contain a mod ID!")
            }

            val modVersion = DefaultArtifactVersion(
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
            )

            // In most cases, Fabric versions of mods share the same mod ID as the Forge variant.
            // We don't want two of the same things, so we shouldn't allow this to occur.
            if (FabricLoaderImpl.INSTANCE.getModCandidate(modId) != null || FabricLoader.getInstance()
                    .isModLoaded(modId)
            ) {
                Kilt.logger.warn("Duplicate Forge and Fabric mod IDs detected: $modId")
                return@forEach
            }

            // Forge and Fabric handle duplicate mods by taking the latest version
            // of the mod, I believe. We should share this behaviour, as some mods may
            // JiJ some other mods.
            if (modLoadingQueue.any { it.modId == modId }) {
                val duplicateMod = modLoadingQueue.first { it.modId == modId }

                if (modVersion > duplicateMod.version) {
                    modLoadingQueue.remove(duplicateMod)
                } else return@forEach // Let's just let it slide.
            }

            // create mod info
            val mod = ForgeMod(
                license = toml.get("license"),
                issueTrackerURL = toml.getOrElse("issueTrackerURL", ""),
                showAsResourcePack = toml.getOrElse("showAsResourcePack", false),
                modId = modId,
                version = modVersion,
                displayName = metadata.getConfigElement<String>("displayName").orElse(modId),
                updateURL = metadata.getConfigElement<String>("updateJSONURL").run {
                    return@run if (this.isPresent)
                        URL(this.get())
                    else
                        null
                },
                credits = metadata.getConfigElement<String>("credits").orElse(""),
                authors = metadata.getConfigElement<String>("authors").orElse(""),
                description = metadata.getConfigElement<String>("description").orElse("MISSING DESCRIPTION")
                    .replace("\r", ""),
                dependencies = mainConfig.getConfigList("dependencies", modId)
                    .map {
                        ForgeMod.ForgeModDependency(
                            modId = it.getConfigElement<String>("modId").orElseThrow {
                                Exception("Forge mod file $fileName's dependencies contains a dependency without a mod ID!")
                            },
                            isMandatory = it.getConfigElement<Boolean>("mandatory").orElse(false),
                            versionRange = MavenVersionAdapter.createFromVersionSpec(
                                it.getConfigElement<String>("versionRange")
                                    .orElseThrow {
                                        Exception("Forge mod file $fileName's dependencies contains a dependency without a version range!")
                                    }
                            ),
                            ordering = IModInfo.Ordering.valueOf(
                                it.getConfigElement<String>("ordering").orElse("NONE")
                            ),
                            side = IModInfo.DependencySide.valueOf(it.getConfigElement<String>("side").orElse("BOTH"))
                        )
                    },
                modFile = modFile?.toFile(),
                modConfig = mainConfig,
                nestedMods = nestedMods,
                // TODO: make logo file square
                logoFile = metadata.getConfigElement<String>("logoFile").orElse(""),
                shouldScan = toml.get("modLoader") as String == "javafml"
            )
            mod.manifest = manifest

            nestedMods.forEach {
                it.parent = mod
            }

            forgeMods.add(mod)
        }

        return forgeMods
    }

    // Remaps all Forge mods from SRG to Intermediary/Yarn/MojMap
    private suspend fun remapMods() {
        DeltaTimeProfiler.push("remapMods")

        val remappedModsDir = (kiltCacheDir / "remappedMods").apply {
            runCatching { createDirectories() }
        }

        val exceptions = KiltRemapper.remapMods(modLoadingQueue, remappedModsDir)

        if (exceptions.isNotEmpty()) {
            FabricGuiEntry.displayError("Errors occurred while remapping Forge mods!", null, {
                val tab = it.addTab("Kilt Error")

                exceptions.forEach { e ->
                    tab.node.addCleanedException(e)
                }

                it.tabs.removeIf { t -> t != tab }
            }, true)
        }

        DeltaTimeProfiler.pop()
    }

    fun loadMods() {
        Kilt.logger.info("Starting initialization of Forge mods...")
        DeltaTimeProfiler.push("loadMods")

        val exceptions = mutableListOf<Exception>()

        fullLoadForgeBuiltin()

        while (modLoadingQueue.isNotEmpty()) {
            try {
                val mod = modLoadingQueue.remove()
                DeltaTimeProfiler.push(mod.modId)

                if (!mod.shouldScan) {
                    mod.scanData = ModFileScanData()
                    mods.add(mod)
                    exceptions.addAll(registerAnnotations(mod, mod.scanData))

                    continue
                }

                val scanData = ModFileScanData()
                scanData.addModFileInfo(ModFileInfo(mod))

                mod.scanData = scanData

                // basically emulate how Forge loads stuff
                try {
                    mod.jar.entries().asIterator().forEach {
                        if (it.name.endsWith(".class")) {
                            val inputStream = mod.jar.getInputStream(it)
                            val visitor = ModClassVisitor()
                            val classReader = ClassReader(inputStream)

                            classReader.accept(visitor, 0)
                            visitor.buildData(scanData.classes, scanData.annotations)
                        }
                    }

                    mods.add(mod)

                    exceptions.addAll(registerAnnotations(mod, scanData))
                } catch (e: Exception) {
                    throw e
                }
            } catch (e: Exception) {
                e.printStackTrace()
                exceptions.add(e)
            } finally {
                DeltaTimeProfiler.pop()
            }
        }

        if (exceptions.isNotEmpty()) {
            FabricGuiEntry.displayError("Errors occurred while loading Forge mods!", null, {
                val tab = it.addTab("Kilt Error")

                exceptions.forEach { e ->
                    tab.node.addCleanedException(e)
                }

                it.tabs.removeIf { t -> t != tab }
            }, true)
        }
        DeltaTimeProfiler.pop()
    }

    private val launcher = FabricLauncherBase.getLauncher()

    private fun registerAnnotations(mod: ForgeMod, scanData: ModFileScanData): List<Exception> {
        val exceptions = mutableListOf<Exception>()

        // Automatically subscribe events
        scanData.annotations
            .filter { it.annotationType == AUTO_SUBSCRIBE_ANNOTATION }
            .forEach {
                // it.annotationData["modid"] as String
                // it.annotationData["bus"] as Mod.EventBusSubscriber.Bus

                try {
                    val modId = it.annotationData["modid"] as String? ?: mod.modId

                    if (modId != mod.modId)
                        return@forEach

                    val busType = Mod.EventBusSubscriber.Bus.valueOf(
                        if (it.annotationData.contains("bus"))
                            (it.annotationData["bus"] as ModAnnotation.EnumHolder).value
                        else "FORGE"
                    )

                    ModLoadingContext.kiltActiveModId = modId
                    busType.bus().get().register(Class.forName(it.clazz.className, true, this::class.java.classLoader))
                    ModLoadingContext.kiltActiveModId = null

                    Kilt.logger.info("Automatically registered event ${it.clazz.className} from mod ID $modId under bus ${busType.name}")
                } catch (e: Exception) {
                    Kilt.logger.error("Failed to register event ${it.clazz.className} from mod ${mod.modId}!")
                    e.printStackTrace()
                    exceptions.add(e)
                }
            }

        return exceptions
    }

    fun initMods() {
        DeltaTimeProfiler.push("initMods")
        val exceptions = mutableListOf<Exception>()

        for (mod in mods) {
            exceptions.addAll(initMod(mod, mod.scanData))
        }

        try {
            ModLoadingStage.CONSTRUCT.deferredWorkQueue.runTasks()
        } catch (e: Exception) {
            exceptions.add(e)
        }

        if (exceptions.isNotEmpty()) {
            FabricGuiEntry.displayError("Errors occurred while initializing Forge mods!", null, {
                val tab = it.addTab("Kilt Error")

                exceptions.forEach { e ->
                    tab.node.addCleanedException(e)
                }

                it.tabs.removeIf { t -> t != tab }
            }, true)
        }
        DeltaTimeProfiler.pop()
    }

    fun initMod(mod: ForgeMod, scanData: ModFileScanData): List<Exception> {
        DeltaTimeProfiler.push(mod.modId)
        val exceptions = mutableListOf<Exception>()

        // this should probably belong to FMLJavaModLanguageProvider, but I doubt there's any mods that use it.
        // I hope.
        scanData.annotations
            .filter { it.annotationType == MOD_ANNOTATION }
            .forEach {
                // it.clazz.className - Class
                // it.annotationData["value"] as String - Mod ID

                try {
                    val modId = it.annotationData["value"] as String

                    if (modId != mod.modId)
                        return@forEach

                    ModLoadingContext.kiltActiveModId = modId

                    val clazz = launcher.loadIntoTarget(it.clazz.className)

                    mod.modObject = clazz.getDeclaredConstructor().newInstance()
                    Kilt.logger.info("Initialized new instance of mod $modId.")

                    ModLoadingContext.kiltActiveModId = null
                } catch (e: Exception) {
                    e.printStackTrace()
                    exceptions.add(e)
                }
            }

        mod.eventBus.post(FMLConstructModEvent(mod, ModLoadingStage.CONSTRUCT))

        DeltaTimeProfiler.pop()
        return exceptions
    }

    private fun loadTransformers(mod: ForgeMod?) {
        if (mod == null) {
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
        return mods.firstOrNull { it.modId == id } ?: modLoadingQueue.firstOrNull { it.modId == id }
    }

    fun hasMod(id: String): Boolean {
        return mods.any { it.modId == id } || modLoadingQueue.any { it.modId == id }
    }

    private var statesProvider: ForgeStatesProvider? = null

    private val fmlPhases = mutableMapOf(
        ModLoadingPhase.LOAD to {
            DeltaTimeProfiler.push("config_load")
            // CONFIG_LOAD
            if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
                ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, FMLPaths.CONFIGDIR.get());
            } else {
                ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.SERVER, FMLPaths.CONFIGDIR.get());
            }
            ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.COMMON, FMLPaths.CONFIGDIR.get());

            // COMMON_SETUP
            DeltaTimeProfiler.popPush("common_setup")
            ModLoader.get()
                .kiltPostEventWrappingModsBuildEvent { FMLCommonSetupEvent(it, ModLoadingStage.COMMON_SETUP) }

            DeltaTimeProfiler.push("runTasks")

            ModLoadingStage.COMMON_SETUP.deferredWorkQueue.runTasks()

            DeltaTimeProfiler.pop()

            // SIDED_SETUP
            DeltaTimeProfiler.popPush("sided_setup")
            ModLoader.get().kiltPostEventWrappingModsBuildEvent {
                if (FabricLoader.getInstance().environmentType == EnvType.CLIENT)
                    FMLClientSetupEvent(it, ModLoadingStage.SIDED_SETUP)
                else
                    FMLDedicatedServerSetupEvent(it, ModLoadingStage.SIDED_SETUP)
            }

            DeltaTimeProfiler.push("runTasks")
            ModLoadingStage.SIDED_SETUP.deferredWorkQueue.runTasks()
            DeltaTimeProfiler.pop()

            // ENQUEUE_IMC
            DeltaTimeProfiler.popPush("enqueue_imc")
            ModLoader.get()
                .kiltPostEventWrappingModsBuildEvent { InterModEnqueueEvent(it, ModLoadingStage.ENQUEUE_IMC) }

            DeltaTimeProfiler.push("runTasks")
            ModLoadingStage.ENQUEUE_IMC.deferredWorkQueue.runTasks()
            DeltaTimeProfiler.pop()

            // PROCESS_IMC
            DeltaTimeProfiler.popPush("process_imc")
            ModLoader.get()
                .kiltPostEventWrappingModsBuildEvent { InterModProcessEvent(it, ModLoadingStage.PROCESS_IMC) }

            DeltaTimeProfiler.push("runTasks")
            ModLoadingStage.PROCESS_IMC.deferredWorkQueue.runTasks()
            DeltaTimeProfiler.pop()

            // COMPLETE
            DeltaTimeProfiler.popPush("complete")
            ModLoader.get().kiltPostEventWrappingModsBuildEvent { FMLLoadCompleteEvent(it, ModLoadingStage.COMPLETE) }

            DeltaTimeProfiler.push("runTasks")
            ModLoadingStage.COMPLETE.deferredWorkQueue.runTasks()
            DeltaTimeProfiler.pop()
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

        DeltaTimeProfiler.push(phase.name.lowercase())
        try {
            fmlPhases[phase]?.invoke()

            for (state in sortedStates) {
                println("running ${state.name()} in ${state.phase()}")

                DeltaTimeProfiler.push(state.name())
                try {
                    state.inlineRunnable().ifPresent { consumer ->
                        consumer.accept(ModList.get())
                    }
                } finally {
                    DeltaTimeProfiler.pop()
                }
            }
        } finally {
            DeltaTimeProfiler.pop()
        }
    }

    internal fun addModToFabric(mod: ForgeMod) {
        modProvider.addModToLoader(mod)
        Kilt.logger.info("Injected mod ${mod.modId} into ${modProvider.name}")
    }

    // We need to initialize all early Forge-related things immediately,
    // because otherwise things will break entirely.
    fun initForge() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap() // fuck you
        ForgeRegistries.init()
    }

    private open class ModLoadingState(val dependency: IModInfo.ModVersion)

    private class IncompatibleDependencyLoadingState(
        dependency: IModInfo.ModVersion,
        val version: ArtifactVersion
    ) : ModLoadingState(dependency) {
        override fun toString(): String {
            return "Incompatible dependency version! (required: ${dependency.versionRange}, found: $version)"
        }
    }

    private class MissingDependencyLoadingState(
        dependency: IModInfo.ModVersion
    ) : ModLoadingState(dependency) {
        override fun toString(): String {
            return "Missing mod ID ${dependency.modId}"
        }
    }

    private class ValidDependencyLoadingState(
        dependency: IModInfo.ModVersion
    ) : ModLoadingState(dependency) {
        override fun toString(): String {
            return "Loaded perfectly fine actually, how do you do?"
        }
    }

    companion object {
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

        private fun isSideValid(side: DependencySide): Boolean {
            if (side == DependencySide.BOTH)
                return true

            return (FabricLoader.getInstance().environmentType == EnvType.CLIENT && side == DependencySide.CLIENT)
                    || (FabricLoader.getInstance().environmentType == EnvType.SERVER && side == DependencySide.SERVER)
        }


    }
}