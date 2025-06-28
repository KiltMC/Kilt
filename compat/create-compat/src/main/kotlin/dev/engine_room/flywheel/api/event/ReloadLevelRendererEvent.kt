package dev.engine_room.flywheel.api.event

import net.minecraft.client.multiplayer.ClientLevel
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.event.IModBusEvent

class ReloadLevelRendererEvent(private val level: ClientLevel) : Event(), IModBusEvent {
    fun level(): ClientLevel {
        return this.level
    }
}