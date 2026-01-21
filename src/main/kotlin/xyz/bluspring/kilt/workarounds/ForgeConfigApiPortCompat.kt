package xyz.bluspring.kilt.workarounds

import net.minecraftforge.fml.config.ModConfig

// Little workaround since I don't want the Forge Config API Port code in this main codebase.
interface ForgeConfigApiPortCompat {
    fun fireConfigLoadEvent(modId: String, config: ModConfig?)
    fun fireConfigReloadEvent(modId: String, config: ModConfig?)
    fun fireConfigUnloadEvent(modId: String, config: ModConfig?)

    companion object : ForgeConfigApiPortCompat {
        val instance: ForgeConfigApiPortCompat? by lazy {
            try {
                Class.forName("xyz.bluspring.kilt.compat.forgeconfig.KiltForgeConfigAPIPortCompat").getDeclaredConstructor().newInstance() as? ForgeConfigApiPortCompat
            } catch (_: Throwable) { null }
        }

        override fun fireConfigLoadEvent(modId: String, config: ModConfig?) {
            instance?.fireConfigLoadEvent(modId, config)
        }

        override fun fireConfigReloadEvent(modId: String, config: ModConfig?) {
            instance?.fireConfigReloadEvent(modId, config)
        }

        override fun fireConfigUnloadEvent(modId: String, config: ModConfig?) {
            instance?.fireConfigUnloadEvent(modId, config)
        }
    }
}
