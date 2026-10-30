package xyz.bluspring.kilt.loader.mod.fabric

import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.config.IConfigEvent
import net.minecraftforge.fml.event.IModBusEvent
import net.minecraftforge.fml.event.config.ModConfigEvent
import net.minecraftforge.fml.loading.moddiscovery.ModInfo
import xyz.bluspring.kilt.workarounds.ForgeConfigApiPortCompat
import java.util.Collections
import net.fabricmc.loader.api.ModContainer as FabricModContainer

class WrappedFabricModContainer(private val fabricContainer: FabricModContainer) : ModContainer(ModInfo(fabricContainer)) {
    private val eventBus = BusBuilder.builder()
        .markerType(IModBusEvent::class.java)
        .setTrackPhases(false)
        .build()

    override fun matches(mod: Any?): Boolean {
        return this.fabricContainer == mod
    }

    override fun getMod(): Any? = this.fabricContainer

    override fun <T> acceptEvent(e: T?) where T : Event?, T : IModBusEvent? {
        when (e) {
            is ModConfigEvent.Loading -> ForgeConfigApiPortCompat.fireConfigLoadEvent(this.modId, e.config)
            is ModConfigEvent.Reloading -> ForgeConfigApiPortCompat.fireConfigReloadEvent(this.modId, e.config)
            is ModConfigEvent.Unloading -> ForgeConfigApiPortCompat.fireConfigUnloadEvent(this.modId, e.config)
        }

        this.eventBus.post(e)
    }

    companion object {
        private val containers: MutableMap<FabricModContainer, WrappedFabricModContainer> = Collections.synchronizedMap(mutableMapOf<FabricModContainer, WrappedFabricModContainer>())

        @JvmStatic
        val wrappedContainers: Collection<WrappedFabricModContainer>
            get() = this.containers.values

        @JvmStatic
        fun get(container: FabricModContainer): WrappedFabricModContainer {
            synchronized(containers) {
                return containers.computeIfAbsent(container, ::WrappedFabricModContainer)
            }
        }
    }
}
