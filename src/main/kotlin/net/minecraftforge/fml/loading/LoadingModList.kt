package net.minecraftforge.fml.loading

import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo
import xyz.bluspring.kilt.Kilt

class LoadingModList {
    fun getModFileById(modid: String): ModFileInfo? {
        val kiltMod = Kilt.loader.getMod(modid) ?: return null

        return kiltMod.owningFile as ModFileInfo
    }
}