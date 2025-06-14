package net.neoforged.fml.config

import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.Bindings

interface IConfigEvent {
    val config: ModConfig?

    fun <T> self(): T where T : Event, T : IConfigEvent {
        return this as T
    }

    @JvmRecord
    data class ConfigConfig(
        val loading: java.util.function.Function<ModConfig, IConfigEvent>,
        val reloading: java.util.function.Function<ModConfig, IConfigEvent>,
        val unloading: java.util.function.Function<ModConfig, IConfigEvent>?,
    )
    companion object {
        @JvmField
        val CONFIGCONFIG = Bindings.getConfigConfiguration().get()

        @JvmStatic
        fun loading(modConfig: ModConfig): IConfigEvent {
            return CONFIGCONFIG.loading.apply(modConfig)
        }

        @JvmStatic
        fun reloading(modConfig: ModConfig): IConfigEvent {
            return CONFIGCONFIG.reloading.apply(modConfig)
        }

        @JvmStatic
        fun unloading(modConfig: ModConfig): IConfigEvent? {
            return CONFIGCONFIG.unloading?.apply(modConfig)
        }
    }
}