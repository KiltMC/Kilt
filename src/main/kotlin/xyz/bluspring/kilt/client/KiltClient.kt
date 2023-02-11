package xyz.bluspring.kilt.client

import net.fabricmc.api.ClientModInitializer
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import xyz.bluspring.kilt.Kilt

class KiltClient : ClientModInitializer {
    override fun onInitializeClient() {
        hasInitialized = true

        Kilt.loader.mods.forEach { mod ->
            mod.eventBus.post(FMLClientSetupEvent(mod, ModLoadingStage.SIDED_SETUP))
        }
    }

    companion object {
        var hasInitialized = false
            private set
    }
}