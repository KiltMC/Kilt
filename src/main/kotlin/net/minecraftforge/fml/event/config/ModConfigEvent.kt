package net.minecraftforge.fml.event.config

import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.config.IConfigEvent
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.IModBusEvent

open class ModConfigEvent(override val config: ModConfig?) : Event(), IModBusEvent, IConfigEvent {
    constructor() : this(null)

    class Loading(config: ModConfig?) : ModConfigEvent(config) {
        constructor() : this(null)
    }
    class Reloading(config: ModConfig?) : ModConfigEvent(config) {
        constructor() : this(null)
    }
    class Unloading(config: ModConfig?) : ModConfigEvent(config) {
        constructor() : this(null)
    }
}