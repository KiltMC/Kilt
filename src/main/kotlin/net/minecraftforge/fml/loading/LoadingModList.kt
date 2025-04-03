package net.minecraftforge.fml.loading

import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo
import net.minecraftforge.fml.loading.moddiscovery.ModInfo
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.mod.fabric.FabricModFileInfoWrapper

class LoadingModList {
    fun getMods(): List<ModInfo> {
        val mods = mutableListOf<ModInfo>()

        for (container in FabricLoader.getInstance().allMods) {
            mods.add(ModInfo(null, container))
        }

        for (mod in Kilt.loader.mods) {
            if (mod == null)
                continue

            mods.add(ModInfo(mod))
        }

        return mods
    }

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