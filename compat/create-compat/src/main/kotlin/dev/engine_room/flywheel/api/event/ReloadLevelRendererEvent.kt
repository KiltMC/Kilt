package dev.engine_room.flywheel.api.event

import net.minecraft.client.multiplayer.ClientLevel
import net.neoforged.bus.api.Event
import net.neoforged.fml.event.IModBusEvent

class ReloadLevelRendererEvent(private val level: ClientLevel?) : Event(), IModBusEvent {
    fun level(): ClientLevel {
        return this.level!!
    }

    constructor() : this(null) {}
}