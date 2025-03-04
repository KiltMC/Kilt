package net.minecraftforge.fml.loading

import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.mod.fabric.FabricModFileInfoWrapper

class LoadingModList {
    fun getModFileById(modid: String): ModFileInfo? {
        val kiltMod = Kilt.loader.getMod(modid)

        if (kiltMod == null && FabricLoader.getInstance().isModLoaded(modid)) {
            return ModFileInfo(null, FabricModFileInfoWrapper(FabricLoader.getInstance().getModContainer(modid).orElseThrow())) // MixinConstraints support....
        } else if (kiltMod == null)
            return null

        return kiltMod.owningFile as ModFileInfo
    }

    companion object {
        private val instance = LoadingModList()

        @JvmStatic
        fun get(): LoadingModList {
            return instance
        }
    }
}