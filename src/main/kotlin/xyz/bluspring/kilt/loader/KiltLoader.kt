package xyz.bluspring.kilt.loader

import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.toml.TomlParser
import com.google.gson.JsonParser
import cpw.mods.modlauncher.Launcher
import cpw.mods.modlauncher.api.IEnvironment
import de.florianmichael.asmfabricloader.api.EarlyRiser
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModMetadata
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.loader.impl.gui.FabricGuiEntry
import net.fabricmc.loader.impl.gui.FabricStatusTree
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.fabricmc.loader.impl.util.FileSystemUtil
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
import net.minecraftforge.forgespi.language.IModInfo.DependencySide
import net.minecraftforge.forgespi.language.MavenVersionAdapter
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.forgespi.locating.ModFileFactory
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.apache.maven.artifact.versioning.VersionRange
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.api.entrypoints.ScanForgeModsEntrypoint
import xyz.bluspring.kilt.loader.asm.AccessTransformerLoader
import xyz.bluspring.kilt.loader.asm.coremod.CoreModLoader
import xyz.bluspring.kilt.loader.mixin.KiltMixinLoader
import xyz.bluspring.kilt.loader.mod.ForgeMod
import xyz.bluspring.kilt.loader.mod.KiltEnvironment
import xyz.bluspring.kilt.loader.mod.KiltModFileFactory
import xyz.bluspring.kilt.loader.mod.LoaderModProvider
import xyz.bluspring.kilt.loader.mod.fabric.FabricModProvider
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.util.*
import java.net.URL
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer
import java.util.jar.JarFile
import java.util.jar.Manifest
import java.util.zip.ZipFile
import kotlin.io.path.*
import kotlin.system.exitProcess

class KiltLoader {
    val mods = LockableObjectArrayList<ForgeMod>()
    internal val forgeMods = ObjectArrayList<ForgeMod>()
    private val tomlParser = TomlParser()

    // I have no fucking clue why this is needed, but for whatever fucking reason,
    // the mods ObjectArrayList is getting resorted *after* it's getting sorted in scanMods,
    // no matter what the fuck I do.
    // I don't have time to deal with this, so this works instead.
    private lateinit var sortedModOrder: Collection<ForgeMod>

    // Meant to be used for compatibility between Fabric and other derivatives of it, such as Quilt.
    // However, I currently haven't found a way to link Kilt's mods into Quilt, so this is how it will
    // be for now.
    val modProvider: LoaderModProvider = FabricModProvider()

    private val environment = KiltEnvironment()

    private data class ModInfo(val id: String, val displayName: String)
    private var hasInjected = false

    suspend fun scanMods() {
        val modLoadingQueue = ConcurrentLinkedQueue<ForgeMod>()
        val fabricModDependencies = mutableMapOf<ModMetadata, Map<String, VersionRange>>()

        // Collect Fabric mods' Forge dependencies
        // FIXME: https://github.com/KiltMC/Kilt/issues/255
        /*for (mod in FabricLoader.getInstance().allMods) {
            if (mod.metadata.containsCustomValue("kilt:forgeDependencies")) {
                fabricModDependencies[mod.metadata] = mod.metadata.getCustomValue("kilt:forgeDependencies").asObject.associate { it.key to VersionRange.createFromVersionSpec(it.value.asString) }
            }
        }*/

        Kilt.logger.info("Scanning the mods directory for Forge mods...")
        DeltaTimeProfiler.push("scanMods")

        val modsDir = FabricLoader.getInstance().gameDir / "mods"

        if (!modsDir.exists() || !modsDir.isDirectory())
            throw IllegalStateException("Mods directory doesn't exist! ...how did you even get to this point?")

        val exception = RuntimeException("Failed to load mods in Kilt!")

        DeltaTimeProfiler.push("preload")
        preloadForgeBuiltinMod(modLoadingQueue)
        modsDir.forEachDirectoryEntry("*.jar") { modFile ->
            try {
                preloadJarMod(modLoadingQueue, modFile, ZipFile(modFile.toFile()))
            } catch (e: Throwable) {
                exception.addSuppressed(e)
            }
        }
        DeltaTimeProfiler.pop()

        try {
            // Allow Fabric mods to call for detecting Forge mods in the loading queue.
            EarlyRiser.invokeEntrypoints("kilt:scanForgeMods", ScanForgeModsEntrypoint::class.java) {
                it.onScanMods(modLoadingQueue)
            }
        } catch (e: Throwable) {
            exception.addSuppressed(e)
        }

        // If exceptions had occurred during preloading, then create a window to show the exceptions.
        if (exception.suppressed.isNotEmpty()) {
            Kilt.logger.error("Failed to load Forge mods in Kilt!")
            exception.printStackTrace()

            FabricGuiEntry.displayError("Exceptions occurred whilst scanning Forge mods in Kilt!", exception, {}, true)

            exitProcess(1)
        }

        Kilt.logger.debug("Re-scanning Forge mods to verify mod dependencies...")

        val mcVersion = DefaultArtifactVersion(
            FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().metadata.version.friendlyString
        )
        val preloadedMods = mutableMapOf<ModInfo, List<DependencyLoadingState>>()
        val modDependencies = mutableMapOf<ModInfo, List<IModInfo.ModVersion>>()
        val modsToRemove = mutableListOf<ModInfo>()

        // Add validation for Forge mods' dependencies
        for (mod in modLoadingQueue) {
            modDependencies[ModInfo(mod.modId, mod.displayName)] = mod.dependencies
        }

        // Add validation for Fabric mods' Forge dependencies
        for ((metadata, dependencies) in fabricModDependencies) {
            val modInfo = ModInfo(metadata.id, metadata.name)
            val deps = mutableListOf<IModInfo.ModVersion>()

            for ((depId, depVersion) in dependencies) {
                deps.add(ForgeMod.ForgeModDependency(depId, depVersion, true, IModInfo.Ordering.NONE, DependencySide.BOTH))
            }

            modDependencies[modInfo] = deps
        }

        // Iterate through the mod loading queue for the first time
        // to validate dependencies.
        modDependencies.forEach { (mod, dependencies) ->
            val states = mutableListOf<DependencyLoadingState>()
            dependencies.forEach dependencies@{ dependency ->
                // I suspect this is how some Forge devs try to handle sided mods.
                if (dependency.modId == "forge" && !isSideValid(dependency.side)) {
                    modsToRemove.add(mod)
                    Kilt.logger.info("Detected that ${mod.displayName} (${mod.id}) may be client-only in a server environment, ignoring.")
                    return@dependencies
                }

                if (!isSideValid(dependency.side))
                    return@dependencies // Don't need to load the dependency.

                if (dependency.modId == "forge") {
                    if (!dependency.versionRange.containsVersion(SUPPORTED_FORGE_API_VERSION)) {
                        states.add(IncompatibleDependencyLoadingState(dependency, SUPPORTED_FORGE_API_VERSION))

                        return@dependencies
                    }

                    states.add(ValidDependencyLoadingState(dependency))

                    return@dependencies
                } else if (dependency.modId == "minecraft") {
                    if (!dependency.versionRange.containsVersion(mcVersion)) {
                        states.add(IncompatibleDependencyLoadingState(dependency, mcVersion))
                        return@dependencies
                    }

                    states.add(ValidDependencyLoadingState(dependency))

                    return@dependencies
                }

                if ( // Check if the dependency exists, and if it's required.
                    modLoadingQueue.none { it.modId == dependency.modId } &&
                    !FabricLoader.getInstance().isModLoaded(dependency.modId) &&
                    dependency.isMandatory
                ) {
                    states.add(MissingDependencyLoadingState(dependency))
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
                        states.add(ValidDependencyLoadingState(dependency))
                    } else {
                        states.add(IncompatibleDependencyLoadingState(dependency, version))
                    }

                    return@dependencies
                } else if (dependencyMod == null) {
                    states.add(MissingDependencyLoadingState(dependency))
                    return@dependencies
                }

                if (!dependency.versionRange.containsVersion(dependencyMod.version)) {
                    states.add(
                        IncompatibleDependencyLoadingState(
                            dependency,
                            dependencyMod.version
                        )
                    )

                    return@dependencies
                }

                states.add(ValidDependencyLoadingState(dependency))
            }

            preloadedMods[mod] = states
        }

        // Remove any mods that shouldn't be handled
        for (mod in modsToRemove) {
            preloadedMods.remove(mod)
            modLoadingQueue.removeIf { it.modId == mod.id }
        }

        // Check if any of the dependencies failed to load
        if (preloadedMods.any { it.value.any { state -> state !is ValidDependencyLoadingState } }) {
            preloadedMods.filter { mod -> mod.value.any { state -> state !is ValidDependencyLoadingState } }
                .forEach { (mod, dependencyStates) ->
                    Kilt.logger.error("${mod.displayName} (${mod.id}) failed to load!")

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
                            "${mod.displayName} (${mod.id}) failed to load!",
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
            Kilt.logger.info("Found ${modLoadingQueue.size} Forge mods.")

            if (modLoadingQueue.isNotEmpty()) {
                try {
                    remapMods(modLoadingQueue)
                } catch (e: Exception) {
                    e.printStackTrace()

                    FabricGuiEntry.displayError("Failed to remap Forge mods!", e, {}, true)

                    exitProcess(1)
                }
            } else {
                Kilt.logger.info("No Forge mods located, not proceeding with mod remapping.")
            }

            modLoadingQueue.forEach { mod ->
                loadTransformers(mod)
                CoreModLoader.scanAndLoadCoreMods(mod)
            }
        }

        sortMods(modLoadingQueue)
        DeltaTimeProfiler.pop()
    }

    private fun sortMods(modLoadingQueue: MutableCollection<ForgeMod>) {
        DeltaTimeProfiler.push("sortMods")
        val graph = modLoadingQueue.buildGraph()
        val sorted = TopologicalSort.topologicalSort(graph, null)

        mods.size(sorted.size)
        sorted.forEachIndexed { i, mod ->
            mods[i] = mod
        }

        mods.freeze()
        modLoadingQueue.clear()

        // See comment at the lateinit
        sortedModOrder = sorted

        DeltaTimeProfiler.pop()
    }

    fun injectMods() {
        if (hasInjected)
            return

        hasInjected = true

        DeltaTimeProfiler.push("addToClassPath")
        for (mod in mods) {
            Kilt.loader.addModToFabric(mod)

            if (mod.modFile != null) { // Avoid adding the Forge builtins
                if (mod.isRemapped()) {
                    FabricLauncherBase.getLauncher().addToClassPath(mod.remappedModFile.toURI().toPath())

                    // Force Java to be aware of the remapped mod
                    FileSystemUtil.getJarFileSystem(mod.remappedModFile.toPath(), true)
                } else // Still need to load JiJ'd libraries
                    FabricLauncherBase.getLauncher().addToClassPath(mod.modFile.toURI().toPath())
            }
        }
        DeltaTimeProfiler.pop()
    }

    fun loadForgeModMixins() {
        KiltMixinLoader.init(mods)
    }

    fun preloadMods() {
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
    }

    private fun preloadForgeBuiltinMod(modLoadingQueue: MutableCollection<ForgeMod>) {
        val forgeMods = if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            val modsList = mutableListOf<ForgeMod>()

            for (url in this::class.java.classLoader.getResources("META-INF/forge.mods.toml")) {
                val toml = tomlParser.parse(url)
                modsList.addAll(parseModsToml(modLoadingQueue, toml, null, null))
            }

            modsList
        } else {
            val kiltFile = KiltLoader::class.java.protectionDomain.codeSource.location.toURI().toPath()
            val kiltJar = JarFile(kiltFile.toFile())

            val toml = tomlParser.parse(kiltJar.getInputStream(kiltJar.getJarEntry("META-INF/forge.mods.toml")))

            parseModsToml(modLoadingQueue, toml, kiltFile, kiltJar)
        }

        // Scan mods for data much earlier
        val scanData = ModFileScanData()

        runBlocking {
            KiltHelper.getForgeClassNodes().asFlow().concurrent().collect {
                val visitor = ModClassVisitor()
                it.accept(visitor)

                visitor.buildData(scanData.classes, scanData.annotations)
            }
        }

        for (mod in forgeMods) {
            scanData.addModFileInfo(ModFileInfo(mod))
            mod.scanData = scanData
        }

        modLoadingQueue.addAll(forgeMods)
        this.forgeMods.addAll(forgeMods)
    }

    // This is used specifically for JiJ'd mods that don't store mods.toml files.
    private fun createCustomMod(modFile: Path, manifest: Manifest?): ForgeMod {
        return ForgeMod(
            "jij_${modFile.nameWithoutExtension.lowercase().replace(Regex("[^a-zA-Z0-9_-]"), "")}",
            "(Kilt JiJ) ${modFile.nameWithoutExtension}",
            description = "This is a JIJ'd (Jar-in-Jar) mod that doesn't contain a mods.toml file, but has been loaded anyway.",
            DefaultArtifactVersion("0.0.0"),
            modFile = modFile.toFile(),
            modConfig = NightConfigWrapper(tomlParser.parse(this::class.java.getResource("/default_mods.toml"))),

            // Registrate and stuff don't provide a mods.toml.....
            shouldScan = manifest?.mainAttributes?.getValue("FMLModType") == "GAMELIBRARY"
        )
    }

    private fun preloadJarMod(
        modLoadingQueue: MutableCollection<ForgeMod>,
        modFile: Path,
        jarFile: ZipFile,
        nestedModUpdater: Consumer<ForgeMod>? = null
    ) {
        // Prevent users from having both Kilt and Connector at the same time.
        if (jarFile.getEntry("org/sinytra/connector/ConnectorUtil.class") != null) {
            throw Exception("Sinytra Connector was detected! I know I said \"Isn't it reasonable to have both?\", but come on!")
        }

        // Avoid loading JiJ'd MixinExtras, we already provide a modern version of it.
        if (jarFile.getEntry("com/llamalad7/mixinextras/injector/ModifyExpressionValue.class") != null) {
            return
        }

        val exception = RuntimeException("Failed to load file ${modFile.name}!")
        var isSpecialCasedFabric = false

        // Do NOT load Fabric mods.
        // Some mod JARs actually store both Forge and Fabric in one JAR by using Forgix.
        // Since Fabric loads the Fabric mod before we can even get to it, we shouldn't load the Forge variant
        // ourselves to avoid mod conflicts. And because Kilt is still in an unstable state.
        if (
            jarFile.getEntry("fabric.mod.json") != null
        ) {
            // Special workaround for Pretty Pipes and other mods that do this kinda shit,
            // because what?
            try {
                val fmjEntry = JsonParser.parseReader(
                    jarFile.getInputStream(jarFile.getEntry("fabric.mod.json")).bufferedReader()
                ).asJsonObject
                val modId = fmjEntry.get("id").asString

                FabricLoader.getInstance().getModContainer(modId).ifPresent { container ->
                    if (container.metadata.dependencies.none { it.modId == "forge" }) {
                        return@ifPresent
                    }

                    isSpecialCasedFabric = true
                }

                if (!isSpecialCasedFabric)
                    return
            } catch (e: Throwable) {
                Kilt.logger.error("Failed to parse FMJ of mod file ${modFile.name}!")
                e.printStackTrace()
                return
            }
        }

        DeltaTimeProfiler.push(modFile.nameWithoutExtension)

        Kilt.logger.debug("Scanning jar file ${modFile.name} for Forge mod metadata.")

        try {
            val modsToml = jarFile.getEntry("META-INF/mods.toml")

            if (nestedModUpdater != null && modsToml == null) {
                // Load the JAR's manifest file, or at least try to.
                val manifest = if (jarFile != null) try {
                    Manifest(jarFile.getInputStream(jarFile.getEntry("META-INF/MANIFEST.MF")))
                } catch (_: Exception) {
                    null
                } else null

                val mod = createCustomMod(modFile, manifest)

                if ((FabricLoader.getInstance()
                        .isModLoaded(mod.modId) || FabricLoaderImpl.INSTANCE.getModCandidate(mod.modId) != null)
                    && mod.modId != "forge"
                    && !isSpecialCasedFabric
                ) {
                    Kilt.logger.warn("Duplicate Forge and Fabric mod IDs detected: ${mod.modId}")
                    return
                }

                // Avoid loading mods twice
                if (modLoadingQueue.any { it.modId == mod.modId })
                    return

                modLoadingQueue.add(mod)

                Kilt.logger.info("Loaded JiJ'd mod ${modFile.nameWithoutExtension}.")
                nestedModUpdater.accept(mod)

                DeltaTimeProfiler.pop()
                return
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
                        if (throwable !is FileAlreadyExistsException && throwable !is java.nio.file.FileAlreadyExistsException && throwable is Exception) {
                            Kilt.logger.error("Failed to load JiJ'd file: $fileName", throwable)
                            exception.addSuppressed(throwable)
                            return@forEach
                        }
                    }

                    preloadJarMod(modLoadingQueue, file, ZipFile(file.toFile())) { mod ->
                        nestedMods.add(mod)
                    }
                }
            }

            val toml = tomlParser.parse(jarFile.getInputStream(modsToml))
            val forgeMods = parseModsToml(modLoadingQueue, toml, modFile, jarFile, nestedMods)

            forgeMods.forEach {
                modLoadingQueue.add(it)
                Kilt.logger.info("Discovered Forge mod ${it.displayName} (${it.modId}) version ${it.version} (${modFile.name})")
            }
        } catch (e: Exception) {
            exception.addSuppressed(e)
            e.printStackTrace()
        }

        DeltaTimeProfiler.pop()

        if (exception.suppressed.isNotEmpty())
            throw exception
    }

    // Split this off from the main preloadMods method, in case it needs to be used again later.
    private fun parseModsToml(
        modLoadingQueue: MutableCollection<ForgeMod>,
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
            if ((FabricLoaderImpl.INSTANCE.getModCandidate(modId) != null || FabricLoader.getInstance()
                    .isModLoaded(modId)) && modId != "forge"
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
                    return@run if (this.isPresent && this.get().isNotBlank())
                        try { URL(this.get()) } catch (_: Throwable) { null }
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
    private suspend fun remapMods(mods: Collection<ForgeMod>) {
        DeltaTimeProfiler.push("remapMods")

        val remappedModsDir = (kiltCacheDir / "remappedMods").apply {
            runCatching { createDirectories() }
        }

        try {
            KiltRemapper.remapMods(mods, remappedModsDir)
        } catch (e: Throwable) {
            e.printStackTrace()
            FabricGuiEntry.displayError("Errors occurred while remapping Forge mods!", e, {}, true)
        }

        DeltaTimeProfiler.pop()
    }

    fun loadMods() {
        Kilt.logger.info("Starting initialization of Forge mods...")
        DeltaTimeProfiler.push("loadMods")

        val exception = RuntimeException("Failed to load Forge mods in Kilt!")

        runBlocking {
            launch(Dispatchers.Default) {
                // TODO: Need to make sure to group mods together so they load in the correct order from each other
                sortedModOrder.asFlow().concurrent()
                    .map { mod ->
                        if (!mod.shouldScan) {
                            mod.scanData = ModFileScanData()
                            return@map mod
                        }

                        if (mod.modFile == null) { // This is usually a Forge built-in, we don't have to worry about scanning this.
                            return@map mod
                        }

                        val scanData = ModFileScanData()
                        scanData.addModFileInfo(ModFileInfo(mod))

                        mod.scanData = scanData

                        val classes = ConcurrentHashMap.newKeySet<ModFileScanData.ClassData>()
                        val annotations = ConcurrentHashMap.newKeySet<ModFileScanData.AnnotationData>()

                        // basically emulate how Forge loads stuff
                        mod.jar.entries().asIterator().asFlow().concurrent()
                            .filter { it.name.endsWith(".class") }
                            .map { withContext(Dispatchers.IO) { mod.jar.getInputStream(it) } }
                            .collect {
                                val visitor = ModClassVisitor()
                                val classReader = ClassReader(it)

                                classReader.accept(visitor, 0)
                                visitor.buildData(classes, annotations)
                            }

                        scanData.classes.addAll(classes)
                        scanData.annotations.addAll(annotations)

                        mod
                    }
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

        if (exception.suppressed.isNotEmpty()) {
            exception.printStackTrace()
            FabricGuiEntry.displayError("Errors occurred while loading Forge mods!", exception, {}, true)
        }
        DeltaTimeProfiler.pop()
    }

    private val launcher = FabricLauncherBase.getLauncher()

    private suspend fun registerAnnotations(mod: ForgeMod, scanData: ModFileScanData) {
        val exception = RuntimeException("Failed to register annotations for mod ${mod.displayName} (${mod.modId})!")

        // Automatically subscribe events
        scanData.annotations.asFlow()
            .filter { it.annotationType == AUTO_SUBSCRIBE_ANNOTATION }
            .collect {
                // it.annotationData["modid"] as String
                // it.annotationData["bus"] as Mod.EventBusSubscriber.Bus

                try {
                    val modId = it.annotationData["modid"] as String?
                        // Use the mod ID of the mod in the class instead
                        ?: scanData.annotations.firstOrNull { a -> checkTypeOrParentsAreType(a.clazz, it.clazz) && a.annotationType == MOD_ANNOTATION }?.annotationData?.get("value") as? String?
                        ?: mod.modId

                    if (modId != mod.modId)
                        return@collect

                    val busType = Mod.EventBusSubscriber.Bus.valueOf(
                        if (it.annotationData.contains("bus"))
                            (it.annotationData["bus"] as ModAnnotation.EnumHolder).value!!
                        else "FORGE"
                    )

                    val dists = if (it.annotationData.contains("value"))
                        (it.annotationData["value"] as List<ModAnnotation.EnumHolder>).map { Dist.valueOf(it.value!!) }
                    else
                        listOf()

                    if (dists.isNotEmpty() && dists.none { DistUtil.distToEnvType(it) == FabricLoader.getInstance().environmentType }) {
                        return@collect
                    }

                    ModLoadingContext.kiltActiveModId = modId
                    busType.bus().get().register(Class.forName(it.clazz.className, true, this::class.java.classLoader))
                    ModLoadingContext.kiltActiveModId = null

                    Kilt.logger.debug("Automatically registered event ${it.clazz.className} from mod ID $modId under bus ${busType.name}")
                } catch (e: Exception) {
                    Kilt.logger.error("Failed to register event ${it.clazz.className} from mod ${mod.modId}!")
                    e.printStackTrace()
                    exception.addSuppressed(e)
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

    fun initMods() {
        DeltaTimeProfiler.push("initMods")
        val exception = RuntimeException("Failed to load Kilt mods!")

        runBlocking {
            sortedModOrder.asFlow()
                .collect { mod ->
                    try {
                        initMod(mod, mod.scanData)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        exception.addSuppressed(e)
                    }
                }
        }

        try {
            ModLoadingStage.CONSTRUCT.deferredWorkQueue.runTasks()
        } catch (e: Exception) {
            e.printStackTrace()
            exception.addSuppressed(e)
        }

        if (exception.suppressed.isNotEmpty()) {
            exception.printStackTrace()
            FabricGuiEntry.displayError("Errors occurred while initializing Forge mods!", exception, {}, true)
        }
        DeltaTimeProfiler.pop()
    }

    suspend fun initMod(mod: ForgeMod, scanData: ModFileScanData) {
        DeltaTimeProfiler.push(mod.modId)
        val exception = RuntimeException("Failed to load mod ${mod.displayName} (${mod.modId})!")

        // this should probably belong to FMLJavaModLanguageProvider, but I doubt there's any mods that use it.
        // I hope.
        var hasInitialized = false
        var hasErrored = false
        scanData.annotations.asFlow()
            .filter { it.annotationType == MOD_ANNOTATION }
            .collect {
                // it.clazz.className - Class
                // it.annotationData["value"] as String - Mod ID

                try {
                    val modId = it.annotationData["value"] as String

                    if (modId != mod.modId)
                        return@collect

                    ModLoadingContext.kiltActiveModId = modId

                    val clazz = launcher.loadIntoTarget(it.clazz.className)

                    try {
                        val constructor = clazz.getDeclaredConstructor(FMLJavaModLoadingContext::class.java)
                        val ctx = FMLJavaModLoadingContext.kiltGetContext(mod)

                        mod.modObject = constructor.newInstance(ctx)
                    } catch (_: NoSuchMethodException) {
                        mod.modObject = clazz.getDeclaredConstructor().newInstance()
                    }

                    Kilt.logger.info("Initialized new instance of mod $modId.")
                    hasInitialized = true

                    ModLoadingContext.kiltActiveModId = null
                } catch (e: Exception) {
                    e.printStackTrace()
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
        mod.eventBus.post(FMLConstructModEvent(mod, ModLoadingStage.CONSTRUCT))
        ModLoadingContext.kiltActiveModId = null

        DeltaTimeProfiler.pop()
    }

    private fun loadTransformers(mod: ForgeMod) {
        if (mod.modFile == null) {
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

    private open class DependencyLoadingState(val dependency: IModInfo.ModVersion)

    private class IncompatibleDependencyLoadingState(
        dependency: IModInfo.ModVersion,
        val version: ArtifactVersion
    ) : DependencyLoadingState(dependency) {
        override fun toString(): String {
            return "Incompatible dependency version! (required: ${dependency.versionRange}, found: $version)"
        }
    }

    private class MissingDependencyLoadingState(
        dependency: IModInfo.ModVersion
    ) : DependencyLoadingState(dependency) {
        override fun toString(): String {
            return "Missing mod ID ${dependency.modId}"
        }
    }

    private class ValidDependencyLoadingState(
        dependency: IModInfo.ModVersion
    ) : DependencyLoadingState(dependency) {
        override fun toString(): String {
            return "Loaded perfectly fine actually, how do you do?"
        }
    }

    companion object {
        @JvmField
        val INSTANCE = KiltLoader()

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