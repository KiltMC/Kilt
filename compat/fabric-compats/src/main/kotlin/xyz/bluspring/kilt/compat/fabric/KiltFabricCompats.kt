package xyz.bluspring.kilt.compat.fabric

import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.kilt.compat.fabric.architectury.KiltArchitecturyApiCompat

class KiltFabricCompats : ModInitializer {
    override fun onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("architectury")) {
            KiltArchitecturyApiCompat.initCommon()
        }
    }
}