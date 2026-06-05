package xyz.bluspring.kilt.loader.mod.fabric

import net.neoforged.bus.api.Event
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.event.IModBusEvent
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.fml.loading.moddiscovery.ModInfo
import xyz.bluspring.kilt.workarounds.ForgeConfigApiPortCompat
import net.fabricmc.loader.api.ModContainer as FabricModContainer

class WrappedFabricModContainer(container: FabricModContainer) : ModContainer(ModInfo(container)) {
    override fun getEventBus(): IEventBus? {
        return null
    }

    override fun <T> acceptEvent(e: T?) where T : Event?, T : IModBusEvent? {
        when (e) {
            is ModConfigEvent.Loading -> ForgeConfigApiPortCompat.fireConfigLoadEvent(this.modId, e.config)
            is ModConfigEvent.Reloading -> ForgeConfigApiPortCompat.fireConfigReloadEvent(this.modId, e.config)
            is ModConfigEvent.Unloading -> ForgeConfigApiPortCompat.fireConfigUnloadEvent(this.modId, e.config)

            else -> super.acceptEvent(e)
        }
    }
}
