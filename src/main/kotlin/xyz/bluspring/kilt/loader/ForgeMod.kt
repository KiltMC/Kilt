package xyz.bluspring.kilt.loader

import cpw.mods.jarhandling.SecureJar
import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener
import net.minecraftforge.fml.event.IModBusEvent
import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.forgespi.locating.ForgeFeature
import net.minecraftforge.forgespi.locating.IModFile
import org.apache.logging.log4j.LogManager
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.VersionRange
import xyz.bluspring.kilt.Kilt
import java.io.File
import java.net.URL
import java.util.Optional
import java.util.function.Supplier
import java.util.jar.JarFile

class ForgeMod(
    val modInfo: ForgeModInfo,
    val modFile: File,
    val modConfig: IConfigurable
) {
    val forgeSpi = ForgeSpiModInfo(this)
    val eventBus: IEventBus = BusBuilder.builder().apply {
        setExceptionHandler(::onEventFailed)
        setTrackPhases(false)
        markerType(IModBusEvent::class.java)
    }.build()

    private fun onEventFailed(
        iEventBus: IEventBus,
        event: Event,
        iEventListeners: Array<IEventListener>,
        i: Int,
        throwable: Throwable
    ) {
        logger.error(EventBusErrorMessage(event, i, iEventListeners, throwable))
    }

    val jar: JarFile
        get() {
            return JarFile(remappedModFile)
        }
    lateinit var remappedModFile: File
    lateinit var scanData: ModFileScanData

    fun getSecureJar(): Supplier<SecureJar> {
        return Supplier {
            SecureJar.from(modFile.toPath())
        }
    }

    // TODO: Make the Kilt mod infos use ForgeSPI's classes
    class ForgeSpiModInfo(val modInfo: ForgeMod) : IModInfo {
        override fun getOwningFile(): IModFileInfo {
            return ForgeSpiModFileInfo(this)
        }

        override fun getModId(): String {
            return modInfo.modInfo.mod.modId
        }

        override fun getDisplayName(): String {
            return modInfo.modInfo.mod.displayName
        }

        override fun getDescription(): String {
            return modInfo.modInfo.mod.description
        }

        override fun getVersion(): ArtifactVersion {
            return modInfo.modInfo.mod.version
        }

        override fun getDependencies(): MutableList<out IModInfo.ModVersion> {
            return modInfo.modInfo.mod.dependencies.map {
                object : IModInfo.ModVersion {
                    override fun getModId(): String {
                        return it.modId
                    }

                    override fun getVersionRange(): VersionRange {
                        return it.versionRange
                    }

                    override fun isMandatory(): Boolean {
                        return it.mandatory
                    }

                    override fun getOrdering(): IModInfo.Ordering {
                        return IModInfo.Ordering.valueOf(it.ordering.name)
                    }

                    override fun getSide(): IModInfo.DependencySide {
                        return IModInfo.DependencySide.valueOf(it.side.name)
                    }

                    override fun setOwner(owner: IModInfo?) {
                    }

                    override fun getOwner(): IModInfo {
                        return this@ForgeSpiModInfo
                    }

                    override fun getReferralURL(): Optional<URL> {
                        return Optional.empty()
                    }

                }
            }.toMutableList()
        }

        override fun getForgeFeatures(): MutableList<out ForgeFeature.Bound> {
            return mutableListOf()
        }

        override fun getNamespace(): String {
            return "kilt"
        }

        override fun getModProperties(): MutableMap<String, Any> {
            return mutableMapOf()
        }

        override fun getUpdateURL(): Optional<URL> {
            return Optional.empty()
        }

        override fun getModURL(): Optional<URL> {
            return Optional.empty()
        }

        override fun getLogoFile(): Optional<String> {
            return Optional.empty()
        }

        override fun getLogoBlur(): Boolean {
            return false
        }

        override fun getConfig(): IConfigurable? {
            return null
        }

    }

    class ForgeSpiModFileInfo(private val main: ForgeSpiModInfo) : IModFileInfo {
        override fun getMods(): MutableList<IModInfo> {
            return mutableListOf(main)
        }

        override fun requiredLanguageLoaders(): MutableList<IModFileInfo.LanguageSpec> {
            return mutableListOf()
        }

        override fun showAsResourcePack(): Boolean {
            return main.modInfo.modInfo.showAsResourcePack
        }

        override fun getFileProperties(): MutableMap<String, Any> {
            return mutableMapOf()
        }

        override fun getLicense(): String {
            return main.modInfo.modInfo.license
        }

        override fun moduleName(): String {
            return main.modInfo.modInfo.mod.displayName
        }

        override fun versionString(): String {
            return main.modInfo.modInfo.mod.version.toString()
        }

        override fun usesServices(): MutableList<String> {
            return mutableListOf()
        }

        override fun getFile(): IModFile? {
            return null
        }

        override fun getConfig(): IConfigurable? {
            return null
        }

    }

    companion object {
        private val logger = LogManager.getLogger()
    }
}
