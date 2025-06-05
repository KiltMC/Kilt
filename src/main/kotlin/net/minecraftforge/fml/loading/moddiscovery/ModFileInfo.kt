package net.minecraftforge.fml.loading.moddiscovery

import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.locating.IModFile
import xyz.bluspring.kilt.loader.mod.ForgeMod
import xyz.bluspring.kilt.loader.mod.fabric.FabricModFileInfoWrapper

open class ModFileInfo(private val kiltMod: ForgeMod?, private val wrapper: FabricModFileInfoWrapper? = null) : IModFileInfo {
    override fun getMods(): MutableList<IModInfo> {
        if (wrapper != null)
            return wrapper.mods

        return mutableListOf(ModInfo(kiltMod!!))
    }

    override fun requiredLanguageLoaders(): MutableList<IModFileInfo.LanguageSpec> {
        return mutableListOf()
    }

    override fun showAsResourcePack(): Boolean {
        if (wrapper != null)
            return wrapper.showAsResourcePack()

        return kiltMod!!.showAsResourcePack
    }

    override fun getFileProperties(): MutableMap<String, Any> {
        if (wrapper != null)
            return wrapper.fileProperties

        return mutableMapOf()
    }

    override fun getLicense(): String {
        if (wrapper != null)
            return wrapper.license!!

        return kiltMod!!.definition.license
    }

    override fun moduleName(): String {
        if (wrapper != null)
            return wrapper.moduleName()!!

        return kiltMod!!.displayName
    }

    override fun versionString(): String {
        if (wrapper != null)
            return wrapper.versionString()!!

        return kiltMod!!.version.toString()
    }

    override fun usesServices(): MutableList<String> {
        return mutableListOf()
    }

    override fun getFile(): IModFile? {
        if (wrapper != null)
            return wrapper.file

        return ModFile(kiltMod!!)
    }

    override fun getConfig(): IConfigurable? {
        if (wrapper != null)
            return wrapper.config

        return kiltMod!!.modConfig
    }
}