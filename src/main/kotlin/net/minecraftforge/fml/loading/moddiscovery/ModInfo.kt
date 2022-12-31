package net.minecraftforge.fml.loading.moddiscovery

import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.locating.ForgeFeature
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.VersionRange
import xyz.bluspring.kilt.loader.ForgeModInfo
import java.net.URL
import java.util.*

class ModInfo(private val kiltModInfo: ForgeModInfo) : IModInfo {
    private val owningFile = ModFileInfo(kiltModInfo)

    override fun getOwningFile(): IModFileInfo {
        return owningFile
    }

    override fun getModId(): String {
        return kiltModInfo.mod.modId
    }

    override fun getDisplayName(): String {
        return kiltModInfo.mod.displayName
    }

    override fun getDescription(): String {
        return kiltModInfo.mod.description
    }

    override fun getVersion(): ArtifactVersion {
        return kiltModInfo.mod.version
    }

    override fun getDependencies(): MutableList<out IModInfo.ModVersion> {
        return kiltModInfo.mod.dependencies.map {
            ModVersion(it)
        }.toMutableList()
    }

    override fun getForgeFeatures(): MutableList<out ForgeFeature.Bound> {
        TODO("Not yet implemented")
    }

    override fun getNamespace(): String {
        TODO("Not yet implemented")
    }

    override fun getModProperties(): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    override fun getUpdateURL(): Optional<URL> {
        TODO("Not yet implemented")
    }

    override fun getModURL(): Optional<URL> {
        TODO("Not yet implemented")
    }

    override fun getLogoFile(): Optional<String> {
        TODO("Not yet implemented")
    }

    override fun getLogoBlur(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getConfig(): IConfigurable {
        TODO("Not yet implemented")
    }

    class ModVersion(private val kiltInfo: ForgeModInfo.ModDependency) : IModInfo.ModVersion {
        private var owner: IModInfo? = null

        override fun getModId(): String {
            return kiltInfo.modId
        }

        override fun getVersionRange(): VersionRange {
            return kiltInfo.versionRange
        }

        override fun isMandatory(): Boolean {
            return kiltInfo.mandatory
        }

        override fun getOrdering(): IModInfo.Ordering {
            return IModInfo.Ordering.valueOf(kiltInfo.ordering.name)
        }

        override fun getSide(): IModInfo.DependencySide {
            return IModInfo.DependencySide.valueOf(kiltInfo.side.name)
        }

        override fun setOwner(owner: IModInfo?) {
            this.owner = owner
        }

        override fun getOwner(): IModInfo? {
            return owner
        }

        override fun getReferralURL(): Optional<URL> {
            return Optional.empty()
        }

    }
}