package xyz.bluspring.kilt.loader

import com.electronwill.nightconfig.toml.TomlParser
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.fabricmc.loader.impl.ModContainerImpl
import net.fabricmc.loader.impl.discovery.ModCandidate
import net.fabricmc.loader.impl.gui.FabricGuiEntry
import net.fabricmc.loader.impl.gui.FabricStatusTree
import net.fabricmc.loader.impl.launch.FabricLauncherBase
import net.minecraft.SharedConstants
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent
import net.minecraftforge.fml.loading.moddiscovery.ModClassVisitor
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo
import net.minecraftforge.forgespi.language.MavenVersionAdapter
import net.minecraftforge.fml.loading.moddiscovery.NightConfigWrapper
import net.minecraftforge.forgespi.language.ModFileScanData
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import xyz.bluspring.kilt.Kilt
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.jar.Manifest
import java.util.zip.ZipFile
import kotlin.system.exitProcess

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
            Kilt.logger.info("Found ${preloadedMods.size} Forge mods. Starting mod loading.")

            loadMods()
        }
    }

    private fun preloadJarMod(modFile: File, jarFile: ZipFile): Map<String, Exception> {
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

            // Load the JAR's manifest file, or at least try to.
            val manifest = try {
                Manifest(jarFile.getInputStream(jarFile.getEntry("META-INF/MANIFEST.MF")))
            } catch (_: Exception) { null }

            val toml = tomlParser.parse(jarFile.getInputStream(modsToml))

            if (toml.get("modLoader") as String != "javafml")
                throw Exception("Forge mod file ${modFile.name} is not a javafml mod!")

            val loaderVersionRange = MavenVersionAdapter.createFromVersionSpec(toml.get("loaderVersion") as String)
            if (!loaderVersionRange.containsVersion(SUPPORTED_FORGE_SPEC_VERSION))
                throw Exception("Forge mod file ${modFile.name} does not support Forge loader version $SUPPORTED_FORGE_SPEC_VERSION (mod supports versions between [$loaderVersionRange]))")

            val mainConfig = NightConfigWrapper(toml)

            val modsMetadataList = mainConfig.getConfigList("mods")

            modsMetadataList.forEach { metadata ->
                val modId = metadata.getConfigElement<String>("modId").orElseThrow {
                    Exception("Forge mod file ${modFile.name} does not contain a mod ID!")
                }

                if (modLoadingQueue.any { it.modInfo.mod.modId == modId })
                    throw IllegalStateException("Duplicate Forge mod ID detected: $modId")

                // create mod info
                val modInfo = ForgeModInfo(
                    license = toml.get("license"),
                    issueTrackerURL = toml.getOrElse("issueTrackerURL", ""),
                    showAsResourcePack = toml.getOrElse("showAsResourcePack", false),
                    mod = ForgeModInfo.ModMetadata(
                        modId,
                        version = DefaultArtifactVersion(
                            metadata.getConfigElement<String>("version").orElse("1")
                                .run {
                                    if (this == "\${file.jarVersion}")
                                        manifest?.mainAttributes?.getValue("Implementation-Version") ?: this
                                    else this
                                }
                        ),
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
                                        Exception("Forge mod file ${modFile.name}'s dependencies contains a dependency without a mod ID!")
                                    },
                                    mandatory = it.getConfigElement<Boolean>("mandatory").orElse(false),
                                    versionRange = MavenVersionAdapter.createFromVersionSpec(
                                        it.getConfigElement<String>("versionRange")
                                            .orElseThrow {
                                                Exception("Forge mod file ${modFile.name}'s dependencies contains a dependency without a version range!")
                                            }
                                    ),
                                    ordering = ForgeModInfo.ModDependency.ModOrdering.valueOf(it.getConfigElement<String>("ordering").orElse("NONE")),
                                    side = ForgeModInfo.ModDependency.ModSide.valueOf(it.getConfigElement<String>("side").orElse("BOTH"))
                                )
                            }
                    )
                )

                modLoadingQueue.add(
                    ForgeMod(
                        modInfo,
                        modFile,
                        mainConfig
                    )
                )

                Kilt.logger.info("Discovered Forge mod ${modInfo.mod.displayName} (${modInfo.mod.modId}) version ${modInfo.mod.version} (${modFile.name})")
            }
        } catch (e: Exception) {
            thrownExceptions[modFile.name] = e
            e.printStackTrace()
        }

        return thrownExceptions
    }

    fun loadMods() {
        Kilt.logger.info("Starting initialization of Forge mods...")

        val launcher = FabricLauncherBase.getLauncher()

        while (modLoadingQueue.isNotEmpty()) {
            val mod = modLoadingQueue.remove()

            // add the mod to the class path
            launcher.addToClassPath(mod.modFile.toPath())

            val scanData = ModFileScanData()
            scanData.addModFileInfo(ModFileInfo(mod))

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

                        val classLoader = launcher.targetClassLoader
                        val clazz = classLoader.loadClass(it.clazz.className)
                        clazz.declaredConstructors[0].newInstance()
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            mods.add(mod)

            mod.eventBus.post(FMLConstructModEvent(mod, ModLoadingStage.CONSTRUCT))
        }
    }

    companion object {
        // These constants are to be updated each time we change versions
        private val SUPPORTED_FORGE_SPEC_VERSION = DefaultArtifactVersion("43") // 1.19.2
        private val SUPPORTED_FORGE_API_VERSION = DefaultArtifactVersion("43.2.2")

        private val MOD_ANNOTATION = Type.getType("Lnet/minecraftforge/fml/common/Mod;")

        private val extractedModsDir = File(FabricLoader.getInstance().gameDir.toFile(), ".kilt/extractedMods").apply {
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