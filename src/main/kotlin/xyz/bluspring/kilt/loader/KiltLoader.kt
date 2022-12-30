package xyz.bluspring.kilt.loader

import com.electronwill.nightconfig.core.UnmodifiableConfig
import com.electronwill.nightconfig.toml.TomlParser
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.impl.FormattedException
import net.fabricmc.loader.impl.discovery.ModResolutionException
import net.fabricmc.loader.impl.gui.FabricGuiEntry
import net.fabricmc.loader.impl.gui.FabricStatusTree
import net.minecraft.SharedConstants
import net.minecraftorge.fml.loading.moddiscovery.NightConfigWrapper
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.util.VersionRange
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.zip.ZipFile
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class KiltLoader {
    private val mods = mutableListOf<ForgeMod>()
    private val modLoadingQueue = ConcurrentLinkedQueue<ForgeMod>()

    fun preloadMods() {
        Kilt.logger.debug("Scanning the mods directory for Forge mods...")

        val modsDir = File(FabricLoader.getInstance().gameDir.toFile(), "mods")

        if (!modsDir.exists() || !modsDir.isDirectory)
            throw IllegalStateException("Mods directory doesn't exist! ...how did you even get to this point?")

        val modFiles = modsDir.listFiles { file -> file.extension == "jar" } ?: throw IllegalStateException("Failed to load mod files!")

        val tomlParser = TomlParser()
        val thrownExceptions = mutableMapOf<String, Exception>()

        // TODO: Make thrown exceptions here create a Fabric window

        // TODO: Implement jar-in-jar mod loading
        modFiles.forEach { modFile ->
            try {
                val jarFile = ZipFile(modFile)
                val modsToml = jarFile.getEntry("META-INF/mods.toml") ?: return@forEach
                val toml = tomlParser.parse(jarFile.getInputStream(modsToml))

                if (toml.get("modLoader") as String != "javafml")
                    throw Exception("Forge mod file ${modFile.name} is not a javafml mod!")

                val loaderVersionRange = VersionRange.parse(toml.get("loaderVersion") as String)
                if (!loaderVersionRange.isInRange(SUPPORTED_FORGE_SPEC_VERSION))
                    throw Exception("Forge mod file ${modFile.name} does not support Forge loader version ${SUPPORTED_FORGE_SPEC_VERSION.friendlyString} (mod supports versions between [$loaderVersionRange]))")

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
                            version = Version.parse(metadata.getConfigElement<String>("version").orElse("1")),
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
                                        versionRange = VersionRange.parse(
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
                            mutableListOf(),
                            modFile
                        )
                    )

                    Kilt.logger.info("Discovered Forge mod ${modInfo.mod.displayName} (${modInfo.mod.modId}) version ${modInfo.mod.version.friendlyString} (${modFile.name})")
                }
            } catch (e: Exception) {
                thrownExceptions[modFile.name] = e
                e.printStackTrace()
            }
        }

        if (thrownExceptions.isNotEmpty()) {
            Kilt.logger.error("Exceptions occurred in Forge mod loading! Creating window..")

            FabricGuiEntry.displayError("Exceptions occurred whilst loading Forge mods in Kilt!", null, {
                val errorTab = it.addTab("Kilt Error")

                thrownExceptions.forEach { (name, exception) ->
                    errorTab.node.addMessage("$name failed to load!", FabricStatusTree.FabricTreeWarningLevel.ERROR)
                        .addCleanedException(exception)
                }

                it.tabs.removeIf { tab -> tab != errorTab }
            }, true)

            exitProcess(1)
        }

        Kilt.logger.debug("Re-scanning Forge mods to verify mod dependencies...")

        // Do this first so the version is actually loaded
        SharedConstants.tryDetectVersion()

        val mcVersion = Version.parse(SharedConstants.getCurrentVersion().name)
        val preloadedMods = mutableMapOf<ForgeMod, List<ModLoadingState>>()
        modLoadingQueue.forEach { mod ->
            val dependencies = mutableListOf<ModLoadingState>()
            mod.modInfo.mod.dependencies.forEach dependencies@{ dependency ->
                if (!isSideValid(dependency.side))
                    return@dependencies // Don't need to load the dependency.

                if (dependency.modId == "forge") {
                    if (!dependency.versionRange.isInRange(SUPPORTED_FORGE_API_VERSION)) {
                        dependencies.add(IncompatibleDependencyLoadingState(
                            dependency,
                            SUPPORTED_FORGE_API_VERSION
                        ))

                        return@dependencies
                    }

                    dependencies.add(ValidDependencyLoadingState(dependency))

                    return@dependencies
                } else if (dependency.modId == "minecraft") {
                    if (!dependency.versionRange.isInRange(mcVersion)) {
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

                if (!dependency.versionRange.isInRange(dependencyMod.modInfo.mod.version)) {
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
        }
    }

    companion object {
        // These constants
        private val SUPPORTED_FORGE_SPEC_VERSION = Version.parse("43") // 1.19.2
        private val SUPPORTED_FORGE_API_VERSION = Version.parse("43.2.2")

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
        val version: Version
    ) : ModLoadingState(dependency) {
        override fun toString(): String {
            return "Incompatible dependency version! (required: ${dependency.versionRange}, found: ${version.friendlyString})"
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