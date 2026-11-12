package xyz.bluspring.kilt.compat.fabric.appleskin

import net.neoforged.bus.api.Event
import net.neoforged.neoforge.common.NeoForge
import squeek.appleskin.api.event.FoodValuesEvent
import squeek.appleskin.api.event.HUDOverlayEvent
import squeek.appleskin.api.event.TooltipOverlayEvent

object KiltAppleSkinCompat {
    fun init() {
        FoodValuesEvent.EVENT.register { event ->
            NeoForge.EVENT_BUS.post(event as Event)
        }

        HUDOverlayEvent.Exhaustion.EVENT.register { event ->
            NeoForge.EVENT_BUS.post(event as Event)
        }

        HUDOverlayEvent.HealthRestored.EVENT.register { event ->
            NeoForge.EVENT_BUS.post(event as Event)
        }

        HUDOverlayEvent.HungerRestored.EVENT.register { event ->
            NeoForge.EVENT_BUS.post(event as Event)
        }

        HUDOverlayEvent.Saturation.EVENT.register { event ->
            NeoForge.EVENT_BUS.post(event as Event)
        }

        TooltipOverlayEvent.Pre.EVENT.register { event ->
            NeoForge.EVENT_BUS.post(event as Event)
        }

        TooltipOverlayEvent.Render.EVENT.register { event ->
            NeoForge.EVENT_BUS.post(event as Event)
        }
    }
}
