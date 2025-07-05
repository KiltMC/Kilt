package xyz.bluspring.kilt.loader.mod

import cpw.mods.jarhandling.SecureJar
import net.neoforged.bus.EventBusErrorMessage
import net.neoforged.bus.api.BusBuilder
import net.neoforged.bus.api.Event
import net.neoforged.bus.api.EventListener
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.loading.moddiscovery.ModFileInfo
import net.neoforged.neoforgespi.language.IConfigurable
import net.neoforged.neoforgespi.language.IModFileInfo
import net.neoforged.neoforgespi.language.IModInfo
import net.neoforged.neoforgespi.language.ModFileScanData
import net.neoforged.neoforgespi.locating.ForgeFeature
import net.neoforged.fml.event.IModBusEvent
import net.neoforged.neoforgespi.language.IModLanguageLoader
import org.apache.logging.log4j.LogManager
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.apache.maven.artifact.versioning.VersionRange
import thedarkcolour.kotlinforforge.neoforge.KotlinModContainer
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.KiltModContainer
import xyz.bluspring.kilt.loader.asm.coremod.CoreMod
import xyz.bluspring.knit.loader.mod.KnitMod
import xyz.bluspring.knit.loader.mod.ModDefinition
import xyz.bluspring.knit.loader.mod.ModDependency
import xyz.bluspring.knit.loader.mod.ModEnvironment
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Path
import java.util.*
import java.util.function.Supplier
import java.util.jar.JarFile
import java.util.jar.Manifest
import kotlin.io.path.toPath

class NeoForgeMod(
    definition: ModDefinition,

    val showAsResourcePack: Boolean = false,
    val modConfig: IConfigurable,
    val modFile: File?,

    private val updateURL: URL? = null,

    val shouldScan: Boolean = true
) : KnitMod(definition), IModInfo {
    private val forgeDependencies = this.definition.dependencies.map { ForgeModDependency(it) }.toMutableList()

    val container = if (definition.additionalData["loader"] == "kotlinforforge")
        KotlinModContainer(this)
    else
        KiltModContainer(this)

    lateinit var scanData: ModFileScanData
    lateinit var modObject: Any

    var parent: NeoForgeMod? = null
    var manifest: Manifest? = null

    val loader = definition.additionalData["loader"] as? String? ?: "javafml"

    private lateinit var secureJar: SecureJar

    val jar: JarFile
        get() {
            return JarFile(modFile)
        }

    val coreMods = mutableListOf<CoreMod>()

    val paths: MutableList<Path>
        get() = mutableListOf<Path>().apply {
            this.add(this@NeoForgeMod.modFile?.toPath() ?: Kilt::class.java.protectionDomain.codeSource.location.toURI().toPath())
        }

    fun getSecureJar(): Supplier<SecureJar> {
        return Supplier {
            if (!this@NeoForgeMod::secureJar.isInitialized) {
                secureJar = SecureJar.from((modFile?.toPath() ?: Kilt::class.java.protectionDomain.codeSource.location.toURI().toPath()))
            }

            return@Supplier secureJar
        }
    }

    fun getFile(name: String): InputStream? {
        return if (modFile == null)
            this::class.java.getResourceAsStream("/$name")
        else {
            val entry = jar.getJarEntry(name) ?: return null
            jar.getInputStream(entry)
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

    override fun getLoader(): IModLanguageLoader? {
        return null
    }

    override fun getModId(): String {
        return this.definition.id
    }

    override fun getDisplayName(): String {
        return this.definition.displayName
    }

    override fun getDescription(): String {
        return this.definition.description
    }

    override fun getVersion(): ArtifactVersion {
        return DefaultArtifactVersion(this.definition.version.toString())
    }

    override fun getDependencies(): MutableList<out IModInfo.ModVersion> {
        return forgeDependencies
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
        return Optional.empty()
    }

    override fun getLogoFile(): Optional<String> {
        return Optional.ofNullable(definition.icon)
    }

    override fun getLogoBlur(): Boolean {
        return false
    }

    override fun getConfig(): IConfigurable {
        return modConfig
    }

    // Event Bus
    private lateinit var lateEventBus: IEventBus

    val eventBus: IEventBus
        get() {
            if (!::lateEventBus.isInitialized) {
                lateEventBus = BusBuilder.builder().apply {
                    setExceptionHandler(::onEventFailed)
                    markerType(IModBusEvent::class.java)
                }.build()
            }

            return lateEventBus
        }

    private fun onEventFailed(
        iEventBus: IEventBus,
        event: Event,
        iEventListeners: Array<EventListener>,
        i: Int,
        throwable: Throwable
    ) {
        logger.error(EventBusErrorMessage(event, i, iEventListeners, throwable))
    }

    class ForgeModDependency(
        private val dependency: ModDependency
    ) : IModInfo.ModVersion {
        private var parent: IModInfo? = null
        private val versionRange = (dependency.constraint as NeoForgeVersionConstraint).range
        private val ordering: IModInfo.Ordering = dependency.additionalData["ordering"] as IModInfo.Ordering
        private val referralUrl: URL? = dependency.additionalData["referralURL"] as? URL?

        override fun getModId(): String {
            return dependency.id
        }

        override fun getVersionRange(): VersionRange {
            return versionRange
        }

        override fun getType(): IModInfo.DependencyType {
            return when (dependency.type) {
                ModDependency.Type.REQUIRED -> IModInfo.DependencyType.REQUIRED
                ModDependency.Type.OPTIONAL -> IModInfo.DependencyType.OPTIONAL
                ModDependency.Type.DISCOURAGED -> IModInfo.DependencyType.DISCOURAGED
                ModDependency.Type.INCOMPATIBLE -> IModInfo.DependencyType.INCOMPATIBLE
            }
        }

        override fun getReason(): Optional<String> {
            return Optional.empty()
        }

        override fun getOrdering(): IModInfo.Ordering {
            return ordering
        }

        override fun getSide(): IModInfo.DependencySide {
            return when (dependency.side) {
                ModEnvironment.BOTH -> IModInfo.DependencySide.BOTH
                ModEnvironment.CLIENT -> IModInfo.DependencySide.CLIENT
                ModEnvironment.SERVER -> IModInfo.DependencySide.SERVER
            }
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
