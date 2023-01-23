package net.minecraftforge.fml.config

import net.minecraftforge.fml.Bindings

interface IConfigEvent {
    val config: ModConfig?

    @JvmRecord
    data class ConfigConfig(
        val loading: java.util.function.Function<ModConfig, IConfigEvent>,
        val reloading: java.util.function.Function<ModConfig, IConfigEvent>
    )
    companion object {
        @JvmField
        val CONFIGCONFIG = Bindings.getConfigConfiguration().get()
    }
}