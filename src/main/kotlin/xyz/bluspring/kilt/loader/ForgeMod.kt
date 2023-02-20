package xyz.bluspring.kilt.loader

import cpw.mods.jarhandling.SecureJar
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.*
import net.fabricmc.loader.api.metadata.version.VersionInterval
import net.fabricmc.loader.api.metadata.version.VersionPredicate
import net.fabricmc.loader.impl.discovery.ModCandidate
import net.fabricmc.loader.impl.metadata.LoaderModMetadata
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
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.apache.maven.artifact.versioning.VersionRange
import java.io.File
import java.net.URL
import java.nio.file.Path
import java.util.Optional
import java.util.function.Supplier
import java.util.jar.JarFile

class ForgeMod(
    val modInfo: ForgeModInfo,
    val modFile: File?,
    val modConfig: IConfigurable
) {
    val forgeSpi = ForgeSpiModInfo(this)
    val eventBus: IEventBus = BusBuilder.builder().apply {
        setExceptionHandler(::onEventFailed)
        setTrackPhases(false)
        markerType(IModBusEvent::class.java)
    }.build()
    val fabricMeta = FabricModMetadata(this)

    private fun onEventFailed(
        iEventBus: IEventBus,
        event: Event,
        iEventListeners: Array<IEventListener>,
        i: Int,
        throwable: Throwable
    ) {
        logger.error(EventBusErrorMessage(event, i, iEventListeners, throwable))
    }

    lateinit var modObject: Any

    val loaderMetadata = KiltFabricModContainer.FabricModMetadata(this)

    private lateinit var internalFabricCandidate: ModCandidate

    val fabricCandidate: ModCandidate
        get() {
            if (!this::internalFabricCandidate.isInitialized)
                internalFabricCandidate = createModCandidate(this)

            return internalFabricCandidate
        }

    private lateinit var internalContainer: KiltModContainer
    val container: KiltModContainer
        get() {
            if (!this::internalContainer.isInitialized)
                internalContainer = KiltModContainer(this)

            return internalContainer
        }

    val jar: JarFile
        get() {
            return JarFile(remappedModFile)
        }
    lateinit var remappedModFile: File
    lateinit var scanData: ModFileScanData

    val paths: MutableList<Path>
        get() = mutableListOf<Path>().apply {
            if (this@ForgeMod::remappedModFile.isInitialized)
                this.add(this@ForgeMod.remappedModFile.toPath())
        }

    fun getSecureJar(): Supplier<SecureJar> {
        return Supplier {
            SecureJar.from(modFile?.toPath())
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

    private class CustomStringValue(private val stringValue: String) : CustomValue {
        override fun getType(): CustomValue.CvType {
            return CustomValue.CvType.STRING
        }

        override fun getAsObject(): CustomValue.CvObject {
            TODO("Not yet implemented")
        }

        override fun getAsArray(): CustomValue.CvArray {
            TODO("Not yet implemented")
        }

        override fun getAsString(): String {
            return stringValue
        }

        override fun getAsNumber(): Number {
            TODO("Not yet implemented")
        }

        override fun getAsBoolean(): Boolean {
            TODO("Not yet implemented")
        }
    }

    private class CustomStringSetValue(vararg set: String) : CustomValue.CvArray {
        private val list = set

        override fun iterator(): MutableIterator<CustomValue> {
            return list.map {
                CustomStringValue(it)
            }.toMutableList().listIterator()
        }

        override fun getType(): CustomValue.CvType {
            return CustomValue.CvType.ARRAY
        }

        override fun getAsObject(): CustomValue.CvObject {
            TODO("Not yet implemented")
        }

        override fun getAsArray(): CustomValue.CvArray {
            return this
        }

        override fun getAsString(): String {
            TODO("Not yet implemented")
        }

        override fun getAsNumber(): Number {
            TODO("Not yet implemented")
        }

        override fun getAsBoolean(): Boolean {
            TODO("Not yet implemented")
        }

        override fun size(): Int {
            return list.size
        }

        override fun get(index: Int): CustomValue {
            return CustomStringValue(list[index])
        }
    }

    private class CustomObjectValue(private val map: MutableMap<String, CustomValue>) : CustomValue.CvObject {
        override fun iterator(): MutableIterator<MutableMap.MutableEntry<String, CustomValue>> {
            return map.iterator()
        }

        override fun getType(): CustomValue.CvType {
            return CustomValue.CvType.OBJECT
        }

        override fun getAsObject(): CustomValue.CvObject {
            return this
        }

        override fun getAsArray(): CustomValue.CvArray {
            TODO("Not yet implemented")
        }

        override fun getAsString(): String {
            TODO("Not yet implemented")
        }

        override fun getAsNumber(): Number {
            TODO("Not yet implemented")
        }

        override fun getAsBoolean(): Boolean {
            TODO("Not yet implemented")
        }

        override fun size(): Int {
            TODO("Not yet implemented")
        }

        override fun containsKey(key: String?): Boolean {
            return map.contains(key)
        }

        override fun get(key: String): CustomValue? {
            return map[key]
        }

    }

    open class FabricModMetadata(private val modInfo: ForgeMod) : ModMetadata {
        private val customValues = mutableMapOf<String, CustomValue>(
            "name" to CustomStringValue(modInfo.modInfo.mod.displayName),
            "description" to CustomStringValue(description),
            "icon" to CustomStringValue(modInfo.modInfo.mod.logoFile)
        )

        override fun getType(): String {
            return "fabric"
        }

        override fun getId(): String {
            return modInfo.modInfo.mod.modId
        }

        override fun getProvides(): MutableCollection<String> {
            return mutableListOf()
        }

        override fun getVersion(): Version {
            return Version.parse(modInfo.modInfo.mod.version.toString())
        }

        override fun getEnvironment(): ModEnvironment {
            return ModEnvironment.UNIVERSAL // TODO: add support for handling this in mods.toml
        }

        override fun getDependencies(): MutableCollection<ModDependency> {
            return mutableListOf() // Already handled by Kilt
        }

        override fun getName(): String {
            return modInfo.modInfo.mod.displayName
        }

        override fun getDescription(): String {
            return modInfo.modInfo.mod.description
        }

        override fun getAuthors(): MutableCollection<Person> {
            return mutableListOf<Person>().apply {
                modInfo.modInfo.mod.authors.split(", ").forEach {
                    this.add(object : Person {
                        override fun getName(): String {
                            return it
                        }

                        override fun getContact(): ContactInformation {
                            return object : ContactInformation {
                                override fun get(key: String?): Optional<String> {
                                    return Optional.empty()
                                }

                                override fun asMap(): MutableMap<String, String> {
                                    return mutableMapOf()
                                }
                            }
                        }

                    })
                }
            }
        }

        override fun getContributors(): MutableCollection<Person> {
            return mutableListOf()
        }

        override fun getContact(): ContactInformation {
            return object : ContactInformation {
                override fun get(key: String?): Optional<String> {
                    return Optional.empty()
                }

                override fun asMap(): MutableMap<String, String> {
                    return mutableMapOf()
                }
            }
        }

        override fun getLicense(): MutableCollection<String> {
            return mutableListOf(modInfo.modInfo.license)
        }

        override fun getIconPath(size: Int): Optional<String> {
            return Optional.ofNullable(modInfo.modInfo.mod.logoFile)
        }

        override fun containsCustomValue(key: String?): Boolean {
            // Trick ModMenu into giving us a Forge badge
            if (key == "patchwork:patcherMeta")
                return true

            return false
        }

        override fun getCustomValue(key: String): CustomValue? {
            return customValues[key]
        }

        override fun getCustomValues(): MutableMap<String, CustomValue> {
            return customValues
        }

        override fun containsCustomElement(key: String?): Boolean {
            return false
        }
    }

    companion object {
        private val logger = LogManager.getLogger()

        private fun createModCandidate(mod: ForgeMod): ModCandidate {
            //createPlain(List<Path> paths, LoaderModMetadata metadata, boolean requiresRemap, Collection<ModCandidate> nestedMods)
            val createPlainMethod = ModCandidate::class.java.getDeclaredMethod("createPlain", List::class.java, LoaderModMetadata::class.java, Boolean::class.java, Collection::class.java)
            createPlainMethod.isAccessible = true

            return createPlainMethod.invoke(this, mod.paths, mod.loaderMetadata, false, mutableListOf<ModCandidate>()) as ModCandidate
        }
    }
}
