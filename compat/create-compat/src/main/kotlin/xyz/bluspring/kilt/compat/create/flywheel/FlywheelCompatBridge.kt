package xyz.bluspring.kilt.compat.create.flywheel

import dev.engine_room.flywheel.api.event.EndClientResourceReloadCallback
import dev.engine_room.flywheel.api.event.EndClientResourceReloadEvent
import dev.engine_room.flywheel.api.event.ReloadLevelRendererCallback
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent
import net.neoforged.fml.ModLoader
import net.neoforged.neoforge.common.NeoForge

object FlywheelCompatBridge {
    fun init() {
        EndClientResourceReloadCallback.EVENT.register { minecraft, resourceManager, initialReload, error ->
            ModLoader.postEventWrapContainerInModOrder(EndClientResourceReloadEvent(minecraft, resourceManager, initialReload, error))
        }

        ReloadLevelRendererCallback.EVENT.register { level ->
            NeoForge.EVENT_BUS.post(ReloadLevelRendererEvent(level))
        }
    }
}
