package xyz.bluspring.kilt.loader

import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.toml.TomlParser
import com.google.gson.JsonParser
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.loader.impl.ModContainerImpl
import net.fabricmc.loader.impl.gui.FabricGuiEntry
import net.fabricmc.loader.impl.gui.FabricStatusTree
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.minecraft.SharedConstants
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent
import net.minecraftforge.fml.loading.moddiscovery.ModClassVisitor
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo
import net.minecraftforge.fml.loading.moddiscovery.NightConfigWrapper
import net.minecraftforge.forgespi.language.MavenVersionAdapter
import net.minecraftforge.forgespi.language.ModFileScanData
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.jar.JarFile
import java.util.jar.Manifest
import java.util.zip.ZipFile
import kotlin.system.exitProcess
import net.minecraftforge.common.ForgeMod as ForgeBuiltinMod

class KiltLoader {
    val mods = mutableListOf<ForgeMod>()
    private val modLoadingQueue = ConcurrentLinkedQueue<ForgeMod>()
    private val tomlParser = TomlParser()

    fun preloadMods() {
        Kilt.logger.info("Scanning the mods directory for Forge mods...")

        val modsDir = File(FabricLoader.getInstance().gameDir.toFile(), "mods")

        if (!modsDir.exists() || !modsDir.isDirectory)
            throw IllegalStateException("Mods directory doesn't exist! ...how did you even get to this point?")

        val modFiles = modsDir.listFiles { file -> file.extension == "jar" } ?: throw IllegalStateException("Failed to load mod files!")

        val thrownExceptions = mutableMapOf<String, Exception>()

        modFiles.forEach { modFile ->
            thrownExceptions.putAll(preloadJarMod(modFile, ZipFile(modFile)))
        }

        // If exceptions had occurred during preloading, then create a window to show the exceptions.
        if (thrownExceptions.isNotEmpty()) {
            Kilt.logger.error("Exceptions occurred in Forge mod loading! Creating window..")

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

        // Do this first so the version is actually loaded
        SharedConstants.tryDetectVersion()

        val mcVersion = DefaultArtifactVersion(SharedConstants.getCurrentVersion().name)
        val preloadedMods = mutableMapOf<ForgeMod, List<ModLoadingState>>()

        // Iterate through the mod loading queue for the first time
        // to validate dependencies.
        modLoadingQueue.forEach { mod ->
            val dependencies = mutableListOf<ModLoadingState>()
            mod.modInfo.mod.dependencies.forEach dependencies@{ dependency ->
                if (!isSideValid(dependency.side))
                    return@dependencies // Don't need to load the dependency.

                if (dependency.modId == "forge") {
                    if (!dependency.versionRange.containsVersion(SUPPORTED_FORGE_API_VERSION)) {
                        dependencies.add(IncompatibleDependencyLoadingState(
                            dependency,
                            SUPPORTED_FORGE_API_VERSION
                        ))

                        return@dependencies
                    }

                    dependencies.add(ValidDependencyLoadingState(dependency))

                    return@dependencies
                } else if (dependency.modId == "minecraft") {
                    if (!dependency.versionRange.containsVersion(mcVersion)) {
                        dependencies.add(IncompatibleDependencyLoadingState(
                            dependency,
                            mcVersion
                        ))

                        return@dependencies
                    }

                    dependencies.add(ValidDependencyLoadingState(dependency))

                    return@dependencies
                }

                if ( // Check if the dependency exists, and if it's required.
                    modLoadingQueue.none { it.modInfo.mod.modId == dependency.modId } &&
                    dependency.mandatory
                ) {
                    dependencies.add(MissingDependencyLoadingState(dependency))
                    return@dependencies
                }

                val dependencyMod = modLoadingQueue.first { it.modInfo.mod.modId == dependency.modId }

                if (!dependency.versionRange.containsVersion(dependencyMod.modInfo.mod.version)) {
                    dependencies.add(IncompatibleDependencyLoadingState(
                        dependency,
                        dependencyMod.modInfo.mod.version
                    ))

                    return@dependencies
                }

                dependencies.add(ValidDependencyLoadingState(dependency))
            }

            preloadedMods[mod] = dependencies
        }

        // Check if any of the dependencies failed to load
        if (preloadedMods.any { it.value.any { state -> state !is ValidDependencyLoadingState } }) {
            Kilt.logger.error("Unloaded dependencies found! Throwing error.")

            FabricGuiEntry.displayError("Incompatible Forge mod set!", null, {
                val tab = it.addTab("Kilt Error")

                preloadedMods.filter { mod -> mod.value.any { state -> state !is ValidDependencyLoadingState } }.forEach { (mod, dependencyStates) ->
                    val message = tab.node.addMessage("${mod.modInfo.mod.displayName} (${mod.modInfo.mod.modId}) failed to load!", FabricStatusTree.FabricTreeWarningLevel.ERROR)

                    dependencyStates.forEach states@{ state ->
                        if (state is ValidDependencyLoadingState)
                            return@states

                        message.addMessage("Dependency ${state.dependency.modId} failed to load: $state", FabricStatusTree.FabricTreeWarningLevel.NONE)
                    }
                }

                it.tabs.removeIf { t -> t != tab }
            }, true)

            exitProcess(1)
        } else {
            Kilt.logger.info("Found ${preloadedMods.size} Forge mods.")

            remapMods()
        }
    }

    // Apparently, Forge has itself as a mod. But Kilt will refuse to handle itself, as it's a Fabric mod.
    // Let's do a trick to load the Forge built-in mod.
    private fun loadForgeBuiltinMod() {
        val forgeMod = if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            val toml = tomlParser.parse(this::class.java.getResource("/META-INF/mods.toml"))
            parseModsToml(toml, null, null).first()
        } else {
            val kiltFile = File(KiltLoader::class.java.protectionDomain.codeSource.location.toURI())
            val kiltJar = JarFile(kiltFile)

            val toml = tomlParser.parse(kiltJar.getInputStream(kiltJar.getJarEntry("META-INF/mods.toml")))

            parseModsToml(toml, kiltFile, kiltJar).first()
        }

        val scanData = ModFileScanData()
        scanData.addModFileInfo(ModFileInfo(forgeMod))

        forgeMod.scanData = scanData

        mods.add(forgeMod)
        addModToFabric(forgeMod)

        forgeMod.modObject = ForgeBuiltinMod()
        forgeMod.eventBus.post(FMLConstructModEvent(forgeMod, ModLoadingStage.CONSTRUCT))
    }

    private fun preloadJarMod(modFile: File, jarFile: ZipFile): Map<String, Exception> {
        // Do NOT load Fabric mods.
        // Some mod JARs actually store both Forge and Fabric in one JAR by using Forgix.
        // Since Fabric loads the Fabric mod before we can even get to it, we shouldn't load the Forge variant
        // ourselves to avoid mod conflicts. And because Kilt is still in an unstable state.
        if (
            jarFile.getEntry("fabric.mod.json") != null
        )
            return mapOf()

        val thrownExceptions = mutableMapOf<String, Exception>()

        Kilt.logger.debug("Scanning jar file ${modFile.name} for Forge mod metadata.")

        try {
            val modsToml = jarFile.getEntry("META-INF/mods.toml") ?: return mapOf()

            // Check for Forge's method of include.
            // Doing it this way is probably faster than scanning the entire JAR.
            val jarJarMetadata = jarFile.getEntry("META-INF/jarjar/metadata.json")

            if (jarJarMetadata != null) {
                val json = JsonParser.parseReader(jarFile.getInputStream(jarJarMetadata).reader()).asJsonObject

                json.getAsJsonArray("jars").forEach {
                    val data = it.asJsonObject
                    val filePath = data.get("path").asString

                    val entry = jarFile.getEntry(filePath) ?: return@forEach

                    // Use the CRC as a way of having a unique point of storage, so
                    // if the file already exists, no need to extract it again.
                    val file = File(extractedModsDir, "${entry.crc}-${filePath.replace("META-INF/jarjar/", "")}")
                    if (!file.exists()) {
                        // Extract the JAR out of its containing mod.
                        file.createNewFile()
                        file.writeBytes(jarFile.getInputStream(entry).readAllBytes())
                    }

                    preloadJarMod(file, ZipFile(file))
                }
            }

            val toml = tomlParser.parse(jarFile.getInputStream(modsToml))

            val forgeMods = parseModsToml(toml, modFile, jarFile)

            forgeMods.forEach {
                modLoadingQueue.add(it)
                Kilt.logger.info("Discovered Forge mod ${it.modInfo.mod.displayName} (${it.modInfo.mod.modId}) version ${it.modInfo.mod.version} (${modFile.name})")
            }
        } catch (e: Exception) {
            thrownExceptions[modFile.name] = e
            e.printStackTrace()
        }

        return thrownExceptions
    }

    // Split this off from the main preloadMods method, in case it needs to be used again later.
    private fun parseModsToml(toml: CommentedConfig, modFile: File?, jarFile: ZipFile?): List<ForgeMod> {
        if (toml.get("modLoader") as String != "javafml")
            throw Exception("Forge mod file ${modFile?.name ?: "(unknown)"} is not a javafml mod!")

        // Load the JAR's manifest file, or at least try to.
        val manifest = if (jarFile != null) try {
            Manifest(jarFile.getInputStream(jarFile.getEntry("META-INF/MANIFEST.MF")))
        } catch (_: Exception) { null } else null

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
                        else this
                    }
            )

            // In most cases, Fabric versions of mods share the same mod ID as the Forge variant.
            // We don't want two of the same things, so we shouldn't allow this to occur.
            if (FabricLoaderImpl.INSTANCE.getModCandidate(modId) != null)
                throw IllegalStateException("Duplicate Forge and Fabric mod IDs detected: $modId")

            // Forge and Fabric handle duplicate mods by taking the latest version
            // of the mod, I believe. We should share this behaviour, as some mods may
            // JiJ some other mods.
            if (modLoadingQueue.any { it.modInfo.mod.modId == modId }) {
                val duplicateMod = modLoadingQueue.first { it.modInfo.mod.modId == modId }

                if (modVersion > duplicateMod.modInfo.mod.version) {
                    modLoadingQueue.remove(duplicateMod)
                } else return@forEach // Let's just let it slide.
            }

            // create mod info
            val modInfo = ForgeModInfo(
                license = toml.get("license"),
                issueTrackerURL = toml.getOrElse("issueTrackerURL", ""),
                showAsResourcePack = toml.getOrElse("showAsResourcePack", false),
                mod = ForgeModInfo.ModMetadata(
                    modId,
                    version = modVersion,
                    displayName = metadata.getConfigElement<String>("displayName").orElse(modId),
                    updateJSONURL = metadata.getConfigElement<String>("updateJSONURL").orElse(""),
                    logoFile = metadata.getConfigElement<String>("logoFile").orElse(""),
                    credits = metadata.getConfigElement<String>("credits").orElse(""),
                    authors = metadata.getConfigElement<String>("authors").orElse(""),
                    description = metadata.getConfigElement<String>("logoFile").orElse("MISSING DESCRIPTION"),
                    displayTest = ForgeModInfo.ModMetadata.DisplayTest.valueOf(metadata.getConfigElement<String>("displayTest").orElse("MATCH_VERSION")),
                    dependencies = mainConfig.getConfigList("dependencies", modId)
                        .map {
                            ForgeModInfo.ModDependency(
                                modId = it.getConfigElement<String>("modId").orElseThrow {
                                    Exception("Forge mod file $fileName's dependencies contains a dependency without a mod ID!")
                                },
                                mandatory = it.getConfigElement<Boolean>("mandatory").orElse(false),
                                versionRange = MavenVersionAdapter.createFromVersionSpec(
                                    it.getConfigElement<String>("versionRange")
                                        .orElseThrow {
                                            Exception("Forge mod file $fileName's dependencies contains a dependency without a version range!")
                                        }
                                ),
                                ordering = ForgeModInfo.ModDependency.ModOrdering.valueOf(it.getConfigElement<String>("ordering").orElse("NONE")),
                                side = ForgeModInfo.ModDependency.ModSide.valueOf(it.getConfigElement<String>("side").orElse("BOTH"))
                            )
                        }
                )
            )

            forgeMods.add(
                ForgeMod(
                    modInfo,
                    modFile,
                    mainConfig
                )
            )
        }

        return forgeMods
    }

    // Remaps all Forge mods from SRG to Intermediary/Yarn/MojMap
    private fun remapMods() {
        val remappedModsDir = File(kiltCacheDir, "remappedMods").apply {
            if (!this.exists())
                this.mkdirs()
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
    }

    fun loadMods() {
        Kilt.logger.info("Starting initialization of Forge mods...")

        val launcher = FabricLauncherBase.getLauncher()
        val exceptions = mutableListOf<Exception>()

        loadForgeBuiltinMod()

        while (modLoadingQueue.isNotEmpty()) {
            try {
                val mod = modLoadingQueue.remove()

                // add the mod to the class path
                launcher.addToClassPath(mod.remappedModFile.toPath())

                val scanData = ModFileScanData()
                scanData.addModFileInfo(ModFileInfo(mod))

                mod.scanData = scanData

                addModToFabric(mod)

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

                    // this should probably belong to FMLJavaModLanguageProvider, but I doubt there's any mods that use it.
                    // I hope.

                    scanData.annotations
                        .filter { it.annotationType == MOD_ANNOTATION }
                        .forEach {
                            // it.clazz.className - Class
                            // it.annotationData["value"] as String - Mod ID

                            try {
                                val clazz = Class.forName(it.clazz.className)
                                mod.modObject = clazz.getDeclaredConstructor().newInstance()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                exceptions.add(e)
                            }
                        }
                } catch (e: Exception) {
                    throw e
                }

                mods.add(mod)

                mod.eventBus.post(FMLConstructModEvent(mod, ModLoadingStage.CONSTRUCT))
            } catch (e: Exception) {
                e.printStackTrace()
                exceptions.add(e)
            }
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
    }

    fun postEvent(ev: Event) {
        mods.forEach {
            it.eventBus.post(ev)
        }
    }

    fun getMod(id: String): ForgeMod? {
        return mods.firstOrNull { it.modInfo.mod.modId == id }
    }

    private fun addModToFabric(mod: ForgeMod) {
        FabricLoaderImpl.INSTANCE.modsInternal.add(mod.container.fabricModContainer)

        val modMapField = FabricLoaderImpl::class.java.getDeclaredField("modMap")
        modMapField.isAccessible = true
        val modMap = modMapField.get(FabricLoaderImpl.INSTANCE) as MutableMap<String, ModContainerImpl>

        modMap[mod.modInfo.mod.modId] = mod.container.fabricModContainer
    }

    companion object {
        // These constants are to be updated each time we change versions
        val SUPPORTED_FORGE_SPEC_VERSION = DefaultArtifactVersion("43") // 1.19.2
        val SUPPORTED_FORGE_API_VERSION = DefaultArtifactVersion("43.2.2")

        private val MOD_ANNOTATION = Type.getType("Lnet/minecraftforge/fml/common/Mod;")

        val kiltCacheDir = File(FabricLoader.getInstance().gameDir.toFile(), ".kilt").apply {
            if (!this.exists())
                this.mkdirs()
        }
        private val extractedModsDir = File(kiltCacheDir, "extractedMods").apply {
            if (!this.exists())
                this.mkdirs()
        }

        private fun isSideValid(side: ForgeModInfo.ModDependency.ModSide): Boolean {
            if (side == ForgeModInfo.ModDependency.ModSide.BOTH)
                return true

            return (FabricLoader.getInstance().environmentType == EnvType.CLIENT && side == ForgeModInfo.ModDependency.ModSide.CLIENT)
                    || (FabricLoader.getInstance().environmentType == EnvType.SERVER && side == ForgeModInfo.ModDependency.ModSide.SERVER)
        }
    }

    private open class ModLoadingState(val dependency: ForgeModInfo.ModDependency)

    private class IncompatibleDependencyLoadingState(
        dependency: ForgeModInfo.ModDependency,
        val version: ArtifactVersion
    ) : ModLoadingState(dependency) {
        override fun toString(): String {
            return "Incompatible dependency version! (required: ${dependency.versionRange}, found: $version)"
        }
    }

    private class MissingDependencyLoadingState(
        dependency: ForgeModInfo.ModDependency
    ) : ModLoadingState(dependency) {
        override fun toString(): String {
            return "Missing mod ID ${dependency.modId}"
        }
    }

    private class ValidDependencyLoadingState(
        dependency: ForgeModInfo.ModDependency
    ) : ModLoadingState(dependency) {
        override fun toString(): String {
            return "Loaded perfectly fine actually, how do you do?"
        }
    }
}