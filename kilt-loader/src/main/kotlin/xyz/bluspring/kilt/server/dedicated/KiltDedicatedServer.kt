package xyz.bluspring.kilt.server.dedicated

import net.fabricmc.api.DedicatedServerModInitializer
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import xyz.bluspring.kilt.Kilt

class KiltDedicatedServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        Kilt.loader.mods.forEach { mod ->
            mod.eventBus.post(FMLDedicatedServerSetupEvent(mod, ModLoadingStage.SIDED_SETUP))
        }
    }
}