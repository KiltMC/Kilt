package xyz.bluspring.kilt.compat.forgeconfig

import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents
import net.minecraftforge.fml.config.ModConfig
import xyz.bluspring.kilt.workarounds.ForgeConfigApiPortCompat

class KiltForgeConfigAPIPortCompat : ForgeConfigApiPortCompat {
    override fun fireConfigLoadEvent(modId: String, config: ModConfig?) {
        ModConfigEvents.loading(modId).invoker().onModConfigLoading(config)
    }

    override fun fireConfigReloadEvent(modId: String, config: ModConfig?) {
        ModConfigEvents.reloading(modId).invoker().onModConfigReloading(config)
    }

    override fun fireConfigUnloadEvent(modId: String, config: ModConfig?) {
        ModConfigEvents.unloading(modId).invoker().onModConfigUnloading(config)
    }
}