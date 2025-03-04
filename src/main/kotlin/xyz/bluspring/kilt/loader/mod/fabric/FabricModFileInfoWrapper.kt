package xyz.bluspring.kilt.loader.mod.fabric

import net.fabricmc.loader.api.ModContainer
import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.locating.IModFile

class FabricModFileInfoWrapper(val mod: ModContainer) : IModFileInfo {
    override fun getMods(): MutableList<IModInfo> {
        return mutableListOf()
    }

    override fun requiredLanguageLoaders(): List<IModFileInfo.LanguageSpec> {
        return emptyList()
    }

    override fun showAsResourcePack(): Boolean {
        return false
    }

    override fun getFileProperties(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    override fun getLicense(): String? {
        return mod.metadata.license.joinToString(",")
    }

    override fun moduleName(): String? {
        return mod.metadata.id
    }

    override fun versionString(): String? {
        return mod.metadata.version.friendlyString
    }

    override fun usesServices(): List<String?>? {
        return emptyList()
    }

    override fun getFile(): IModFile? {
        return null
    }

    override fun getConfig(): IConfigurable? {
        return null
    }
}