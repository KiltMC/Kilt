package xyz.bluspring.kilt.compat.create

import net.createmod.ponder.PonderClient
import net.fabricmc.loader.api.FabricLoader
import net.neoforged.fml.ModContainer
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.api.KiltWrappedModContainerEntrypoint
import xyz.bluspring.kilt.api.compatibility.KiltModCompatBridgeManager

class KiltCreateCompatWrappedNeoContainer : KiltWrappedModContainerEntrypoint {
    override fun onLoadModContainer(container: ModContainer) {
        if ((FabricLoader.getInstance().isModLoaded("ponder") && !Kilt.loader.hasMod("ponder")) || KiltModCompatBridgeManager.isActive("ponder")) {
            container.eventBus!!.addListener { _: FMLLoadCompleteEvent ->
                // Initialize Ponder Fabric later
                PonderClient.modLoadCompleted()
            }
        }
    }
}
