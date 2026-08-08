package xyz.bluspring.mods.flashbacksablecompat

import net.fabricmc.loader.api.FabricLoader

object ModSupport {
    @JvmField val SABLE_LOADED = FabricLoader.getInstance().isModLoaded("sable")
    @JvmField val FLASHBACK_LOADED = FabricLoader.getInstance().isModLoaded("flashback")
}
