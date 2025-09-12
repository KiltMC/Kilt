package net.minecraftforge.fml.loading

import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo
import net.minecraftforge.fml.loading.moddiscovery.ModInfo
import xyz.bluspring.kilt.Kilt

class LoadingModList {
    // Forge agree on a place to hold mods challenge [impossible]
    val mods: List<ModInfo>
        get() {
            return Kilt.loader.mods.map { ModInfo(it, null) }
        }

    fun getModFileById(modid: String): ModFileInfo? {
        val kiltMod = Kilt.loader.getMod(modid) ?: return null

        return kiltMod.owningFile as ModFileInfo
    }

    companion object {
        val instance = LoadingModList()

        @JvmStatic
        fun get(): LoadingModList {
            return instance
        }
    }
}