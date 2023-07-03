package net.minecraftforge.fml.loading.moddiscovery

import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.locating.ForgeFeature
import org.apache.maven.artifact.versioning.ArtifactVersion
import xyz.bluspring.kilt.loader.mod.ForgeMod
import java.net.URL
import java.util.*

class ModInfo(private val kiltModInfo: ForgeMod) : IModInfo {
    private val owningFile = ModFileInfo(kiltModInfo)

    override fun getOwningFile(): IModFileInfo {
        return owningFile
    }

    override fun getModId(): String {
        return kiltModInfo.modId
    }

    override fun getDisplayName(): String {
        return kiltModInfo.displayName
    }

    override fun getDescription(): String {
        return kiltModInfo.description
    }

    override fun getVersion(): ArtifactVersion {
        return kiltModInfo.version
    }

    override fun getDependencies(): MutableList<out IModInfo.ModVersion> {
        return kiltModInfo.dependencies.toMutableList()
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
}