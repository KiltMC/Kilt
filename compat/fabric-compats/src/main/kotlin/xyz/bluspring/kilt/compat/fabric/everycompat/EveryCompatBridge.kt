package xyz.bluspring.kilt.compat.fabric.everycompat

import net.fabricmc.loader.api.FabricLoader
import net.mehvahdjukaar.every_compat.neoforge.EveryCompatForge
import xyz.bluspring.kilt.loader.mod.fabric.WrappedFabricModContainer

object EveryCompatBridge {
    val container = WrappedFabricModContainer.get(FabricLoader.getInstance().getModContainer("everycomp").orElseThrow())
    val forgeInstance = EveryCompatForge(this.container.eventBus)

    fun init() {}
}
