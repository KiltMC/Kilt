package xyz.bluspring.kilt.loader.mod

import cpw.mods.jarhandling.SecureJar
import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener
import net.minecraftforge.fml.event.IModBusEvent
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo
import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.forgespi.locating.ForgeFeature
import org.apache.logging.log4j.LogManager
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.VersionRange
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.KiltModContainer
import java.io.File
import java.net.URL
import java.nio.file.Path
import java.util.*
import java.util.function.Supplier
import java.util.jar.JarFile
import java.util.jar.Manifest
import kotlin.io.path.toPath

class ForgeMod(
    private val modId: String,
    private val displayName: String,
    private val description: String,
    private val version: ArtifactVersion,
    private val dependencies: List<ForgeModDependency> = listOf(),

    private val modURL: URL? = null,
    private val logoFile: String? = null,

    val nestedMods: List<ForgeMod> = listOf(),

    val showAsResourcePack: Boolean = false,
    val license: String = "All Rights Reserved",
    val modConfig: IConfigurable,
    val modFile: File?,

    val issueTrackerURL: String = "",
    private val updateURL: URL? = null,

    val authors: String = "",
    val credits: String = ""
) : IModInfo {
    val container = KiltModContainer(this)

    lateinit var remappedModFile: File
    lateinit var scanData: ModFileScanData
    lateinit var modObject: Any

    var parent: ForgeMod? = null
    var manifest: Manifest? = null

    val jar: JarFile
        get() {
            return if (this@ForgeMod::remappedModFile.isInitialized)
                JarFile(remappedModFile)
            else
                JarFile(modFile)
        }

    fun isRemapped(): Boolean {
        return this@ForgeMod::remappedModFile.isInitialized
    }

    val paths: MutableList<Path>
        get() = mutableListOf<Path>().apply {
            if (this@ForgeMod::remappedModFile.isInitialized)
                this.add(this@ForgeMod.remappedModFile.toPath())
            else
                this.add(this@ForgeMod.modFile?.toPath() ?: Kilt::class.java.protectionDomain.codeSource.location.toURI().toPath())
        }

    fun getSecureJar(): Supplier<SecureJar> {
        return Supplier {
            if (this@ForgeMod::remappedModFile.isInitialized)
                SecureJar.from(remappedModFile.toPath())
            else
                SecureJar.from((modFile?.toPath() ?: Kilt::class.java.protectionDomain.codeSource.location.toURI().toPath()))
        }
    }

    init {
        this.dependencies.forEach {
            it.owner = this
        }
    }

    // Forge SPI reimpls
    override fun getOwningFile(): IModFileInfo {
        return ModFileInfo(this)
    }

    override fun getModId(): String {
        return modId
    }

    override fun getDisplayName(): String {
        return displayName
    }

    override fun getDescription(): String {
        return description
    }

    override fun getVersion(): ArtifactVersion {
        return version
    }

    override fun getDependencies(): MutableList<out IModInfo.ModVersion> {
        return dependencies.toMutableList()
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
        return Optional.ofNullable(updateURL)
    }

    override fun getModURL(): Optional<URL> {
        return Optional.ofNullable(modURL)
    }

    override fun getLogoFile(): Optional<String> {
        return Optional.ofNullable(logoFile)
    }

    override fun getLogoBlur(): Boolean {
        return false
    }

    override fun getConfig(): IConfigurable {
        return modConfig
    }

    // Event Bus
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

    class ForgeModDependency(
        private val modId: String,
        private val versionRange: VersionRange,
        private val isMandatory: Boolean,
        private val ordering: IModInfo.Ordering,
        private val side: IModInfo.DependencySide,
        private val referralUrl: URL? = null
    ) : IModInfo.ModVersion {
        private var parent: IModInfo? = null

        override fun getModId(): String {
            return modId
        }

        override fun getVersionRange(): VersionRange {
            return versionRange
        }

        override fun isMandatory(): Boolean {
            return isMandatory
        }

        override fun getOrdering(): IModInfo.Ordering {
            return ordering
        }

        override fun getSide(): IModInfo.DependencySide {
            return side
        }

        override fun setOwner(owner: IModInfo?) {
            parent = owner
        }

        override fun getOwner(): IModInfo? {
            return parent
        }

        override fun getReferralURL(): Optional<URL> {
            return Optional.ofNullable(referralUrl)
        }
    }

    companion object {
        private val logger = LogManager.getLogger()
    }
}
