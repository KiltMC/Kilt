package xyz.bluspring.kilt.server.dedicated

import net.fabricmc.api.DedicatedServerModInitializer

class KiltDedicatedServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        /*Kilt.loader.mods.forEach { mod ->
            mod.eventBus.post(FMLDedicatedServerSetupEvent(mod, ModLoadingStage.SIDED_SETUP))
        }

        ModLoadingStage.SIDED_SETUP.deferredWorkQueue.runTasks()*/
    }
}