package xyz.bluspring.kilt.loader.mod.fabric

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.loading.moddiscovery.ModInfo
import net.fabricmc.loader.api.ModContainer as FabricModContainer

class WrappedFabricModContainer(container: FabricModContainer) : ModContainer(ModInfo(container)) {
    override fun getEventBus(): IEventBus? {
        return null
    }
}