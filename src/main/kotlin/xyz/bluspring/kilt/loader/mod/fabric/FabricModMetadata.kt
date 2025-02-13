package xyz.bluspring.kilt.loader.mod.fabric

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.*
import net.fabricmc.loader.impl.metadata.EntrypointMetadata
import net.fabricmc.loader.impl.metadata.LoaderModMetadata
import net.fabricmc.loader.impl.metadata.NestedJarEntry
import xyz.bluspring.kilt.loader.mod.ForgeMod
import java.util.*
import kotlin.jvm.optionals.getOrNull

class FabricModMetadata(private val mod: ForgeMod) : ModMetadata, LoaderModMetadata {
    private val customValues = mutableMapOf<String, CustomValue>(
        "name" to CustomStringValue(mod.displayName),
        "description" to CustomStringValue(description),
        "icon" to CustomStringValue(mod.logoFile.getOrNull() ?: "")
    )

    override fun getType(): String {
        return "forge"
    }

    override fun getId(): String {
        return mod.modId
    }

    override fun getProvides(): MutableCollection<String> {
        return mod.nestedMods.map {
            it.modId
        }.toMutableList()
    }

    override fun getVersion(): Version {
        return Version.parse(mod.version.toString())
    }

    override fun getEnvironment(): ModEnvironment {
        return ModEnvironment.UNIVERSAL // TODO: add support for handling this in mods.toml
    }

    override fun getDependencies(): MutableCollection<ModDependency> {
        return mutableListOf() // Already handled by Kilt
    }

    override fun getName(): String {
        return mod.displayName
    }

    override fun getDescription(): String {
        return mod.description
    }

    override fun getAuthors(): MutableCollection<Person> {
        return mutableListOf<Person>().apply {
            mod.authors.split(",").forEach {
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
        return mutableListOf(mod.license)
    }

    override fun getIconPath(size: Int): Optional<String> {
        return mod.logoFile
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

    override fun loadsInEnvironment(type: EnvType?): Boolean {
        return true
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
        return mutableListOf()
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