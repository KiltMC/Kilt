package xyz.bluspring.kilt.compat.fabric

import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.compat.fabric.appleskin.KiltAppleSkinCompat

class KiltFabricCompats : ModInitializer {
    override fun onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("appleskin") && !Kilt.loader.hasMod("appleskin")) {
            KiltAppleSkinCompat.init()
        }
    }
}
