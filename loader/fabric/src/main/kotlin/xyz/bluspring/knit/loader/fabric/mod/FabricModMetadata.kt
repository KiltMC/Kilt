package xyz.bluspring.knit.loader.fabric.mod

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.*
import net.fabricmc.loader.impl.metadata.EntrypointMetadata
import net.fabricmc.loader.impl.metadata.LoaderModMetadata
import net.fabricmc.loader.impl.metadata.NestedJarEntry
import xyz.bluspring.knit.loader.KnitModLoader
import xyz.bluspring.knit.loader.fabric.mod.metadata.CustomStringValue
import xyz.bluspring.knit.loader.fabric.mod.metadata.toMetadataValue
import xyz.bluspring.knit.loader.mod.ModDefinition
import java.util.*
import xyz.bluspring.knit.loader.mod.ModEnvironment as KnitModEnvironment

class FabricModMetadata(private val definition: ModDefinition, private val loader: KnitModLoader<*>) : ModMetadata, LoaderModMetadata {
    private val customValues = mutableMapOf<String, CustomValue>(
        "name" to CustomStringValue(definition.displayName),
        "description" to CustomStringValue(definition.description),
        "icon" to CustomStringValue(definition.icon)
    ).apply {
        for ((key, value) in definition.loaderCustomData) {
            this[key] = value.toMetadataValue()
        }
    }

    override fun getType(): String {
        return loader.supportedLoader.lowercase()
    }

    override fun getId(): String {
        return definition.id
    }

    override fun getProvides(): MutableCollection<String> {
        return mutableListOf()
    }

    override fun getVersion(): Version {
        return Version.parse(definition.version. toString())
    }

    override fun getEnvironment(): ModEnvironment {
        return when (definition.environment) {
            KnitModEnvironment.CLIENT -> ModEnvironment.CLIENT
            KnitModEnvironment.SERVER -> ModEnvironment.SERVER
            KnitModEnvironment.BOTH -> ModEnvironment.UNIVERSAL
        }
    }

    override fun getDependencies(): MutableCollection<ModDependency> {
        return mutableListOf()
    }

    override fun getName(): String {
        return definition.displayName
    }

    override fun getDescription(): String {
        return definition.description
    }

    override fun getAuthors(): MutableCollection<Person> {
        return mutableListOf<Person>().apply {
            definition.authors.forEach {
                this.add(object : Person {
                    override fun getName(): String {
                        return it.trim()
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
        return mutableListOf(definition.license)
    }

    override fun getIconPath(size: Int): Optional<String> {
        return if (definition.icon.isBlank()) Optional.empty() else Optional.of(definition.icon)
    }

    override fun containsCustomValue(key: String?): Boolean {
        return customValues.containsKey(key)
    }

    override fun getCustomValue(key: String): CustomValue? {
        return customValues[key]
    }

    override fun getCustomValues(): MutableMap<String, CustomValue> {
        return customValues
    }

    override fun containsCustomElement(key: String): Boolean {
        return customValues.containsKey(key)
    }

    override fun loadsInEnvironment(type: EnvType?): Boolean {
        return when (type) {
            null -> definition.environment == KnitModEnvironment.BOTH
            EnvType.CLIENT -> definition.environment.supportsClient()
            EnvType.SERVER -> definition.environment.supportsServer()
        }
    }

    override fun getEntrypoints(type: String?): MutableList<EntrypointMetadata> {
        return mutableListOf()
    }

    override fun getEntrypointKeys(): MutableCollection<String> {
        return mutableListOf()
    }

    override fun getSchemaVersion(): Int {
        return 1
    }

    override fun getLanguageAdapterDefinitions(): MutableMap<String, String> {
        return mutableMapOf()
    }

    override fun getJars(): MutableCollection<NestedJarEntry> {
        return mutableListOf()
    }

    override fun getMixinConfigs(type: EnvType?): MutableCollection<String> {
        return definition.mixinConfigs.filter {
            (type == null && it.environment == KnitModEnvironment.BOTH)
                || (type == EnvType.CLIENT && it.environment.supportsClient())
                || (type == EnvType.SERVER && it.environment.supportsServer())
        }
            .map { it.config }
            .toMutableList()
    }

    override fun getAccessWidener(): String? {
        return null
    }

    override fun getOldInitializers(): MutableCollection<String> {
        return mutableListOf()
    }

    override fun emitFormatWarnings() {
    }

    override fun setVersion(version: Version?) {
    }

    override fun setDependencies(dependencies: MutableCollection<ModDependency>?) {
    }
}