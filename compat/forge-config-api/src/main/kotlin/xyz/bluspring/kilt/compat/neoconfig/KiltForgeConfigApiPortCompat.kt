package xyz.bluspring.kilt.compat.neoconfig

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents
import net.neoforged.fml.config.ModConfig
import xyz.bluspring.kilt.workarounds.ForgeConfigApiPortCompat

class KiltForgeConfigApiPortCompat : ForgeConfigApiPortCompat {
    override fun fireConfigLoadEvent(modId: String, config: ModConfig?) {
        NeoForgeModConfigEvents.loading(modId).invoker().onModConfigLoading(config)
    }

    override fun fireConfigReloadEvent(modId: String, config: ModConfig?) {
        NeoForgeModConfigEvents.reloading(modId).invoker().onModConfigReloading(config)
    }

    override fun fireConfigUnloadEvent(modId: String, config: ModConfig?) {
        NeoForgeModConfigEvents.unloading(modId).invoker().onModConfigUnloading(config)
    }
}
