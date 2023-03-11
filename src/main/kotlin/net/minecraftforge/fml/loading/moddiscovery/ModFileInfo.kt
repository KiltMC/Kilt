package net.minecraftforge.fml.loading.moddiscovery

import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.locating.IModFile
import xyz.bluspring.kilt.loader.ForgeMod

open class ModFileInfo(private val kiltMod: ForgeMod) : IModFileInfo {
    private val kiltModInfo = kiltMod.modInfo

    override fun getMods(): MutableList<IModInfo> {
        return mutableListOf(ModInfo(kiltMod))
    }

    override fun requiredLanguageLoaders(): MutableList<IModFileInfo.LanguageSpec> {
        return mutableListOf()
    }

    override fun showAsResourcePack(): Boolean {
        return kiltModInfo.showAsResourcePack
    }

    override fun getFileProperties(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    override fun getLicense(): String {
        return kiltModInfo.license
    }

    override fun moduleName(): String {
        return kiltModInfo.mod.displayName
    }

    override fun versionString(): String {
        return kiltModInfo.mod.version.toString()
    }

    override fun usesServices(): MutableList<String> {
        return mutableListOf()
    }

    override fun getFile(): IModFile {
        return ModFile(kiltMod)
    }

    override fun getConfig(): IConfigurable {
        return kiltMod.modConfig
    }
}