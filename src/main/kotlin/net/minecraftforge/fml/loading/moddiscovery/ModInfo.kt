package net.minecraftforge.fml.loading.moddiscovery

import net.fabricmc.loader.api.ModContainer
import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.locating.ForgeFeature
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import xyz.bluspring.kilt.loader.mod.NeoForgeMod
import xyz.bluspring.kilt.loader.mod.fabric.FabricModFileInfoWrapper
import java.net.URL
import java.util.*

class ModInfo(private val kiltModInfo: NeoForgeMod? = null, private val fabricModContainer: ModContainer? = null) : IModInfo {
    private val owningFile = ModFileInfo(kiltModInfo, if (fabricModContainer != null) FabricModFileInfoWrapper(fabricModContainer) else null)

    override fun getOwningFile(): IModFileInfo {
        return owningFile
    }

    override fun getModId(): String {
        return kiltModInfo?.modId ?: fabricModContainer?.metadata?.id ?: ""
    }

    override fun getDisplayName(): String {
        return kiltModInfo?.displayName ?: fabricModContainer?.metadata?.name ?: ""
    }

    override fun getDescription(): String {
        return kiltModInfo?.description ?: fabricModContainer?.metadata?.description ?: ""
    }

    override fun getVersion(): ArtifactVersion {
        return kiltModInfo?.version ?: DefaultArtifactVersion(fabricModContainer?.metadata?.version?.friendlyString ?: "0.0.0")
    }

    override fun getDependencies(): MutableList<out IModInfo.ModVersion> {
        return kiltModInfo?.dependencies?.toMutableList() ?: mutableListOf()
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
        return Optional.empty()
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

    override fun getConfig(): IConfigurable? {
        return kiltModInfo?.config
    }
}