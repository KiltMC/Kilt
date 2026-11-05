package xyz.bluspring.kilt.loader.mod.fabric

import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.config.IConfigEvent
import net.minecraftforge.fml.event.config.ModConfigEvent
import net.minecraftforge.fml.loading.moddiscovery.ModInfo
import xyz.bluspring.kilt.workarounds.ForgeConfigApiPortCompat
import java.util.Collections
import net.fabricmc.loader.api.ModContainer as FabricModContainer

class WrappedFabricModContainer(val container: FabricModContainer) : ModContainer(
    ModInfo(
        null,
        container
    )
) {
    override fun dispatchConfigEvent(event: IConfigEvent) {
        when (event) {
            is ModConfigEvent.Loading -> {
                ForgeConfigApiPortCompat.fireConfigReloadEvent(this.modId, event.config)
            }

            is ModConfigEvent.Reloading -> {
                ForgeConfigApiPortCompat.fireConfigReloadEvent(this.modId, event.config)
            }

            is ModConfigEvent.Unloading -> {
                ForgeConfigApiPortCompat.fireConfigReloadEvent(this.modId, event.config)
            }
        }
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
