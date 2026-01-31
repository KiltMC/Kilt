package xyz.bluspring.kilt.compat.create

import dev.engine_room.flywheel.api.event.EndClientResourceReloadCallback
import dev.engine_room.flywheel.api.event.EndClientResourceReloadEvent
import dev.engine_room.flywheel.api.event.ReloadLevelRendererCallback
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.neoforged.fml.ModLoader
import net.neoforged.neoforge.common.NeoForge

class KiltCreateCompat : ClientModInitializer {
    override fun onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("flywheel") && FabricLoader.getInstance().getModContainer("flywheel").orElseThrow().metadata.version >= Version.parse("1.0.0")) {
            initFlywheelEvents()
        }
    }

    private fun initFlywheelEvents() {
        EndClientResourceReloadCallback.EVENT.register { minecraft, resourceManager, initialReload, error ->
            NeoForge.EVENT_BUS.post(EndClientResourceReloadEvent(minecraft, resourceManager, initialReload, error))
        }

        ReloadLevelRendererCallback.EVENT.register { level ->
            ModLoader.postEventWrapContainerInModOrder(ReloadLevelRendererEvent(level))
        }
    }
}