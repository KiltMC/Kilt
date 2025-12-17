package xyz.bluspring.kilt.compat.fabric

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.kilt.compat.fabric.geckolib.GeckoLibEvents

class KiltFabricCompatsClient : ClientModInitializer {
    override fun onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("geckolib")) {
            GeckoLibEvents.init()
        }
    }
}