package net.minecraftforge.common.capabilities

import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.event.IModBusEvent
import org.objectweb.asm.Type

class RegisterCapabilitiesEvent : Event(), IModBusEvent {
    fun <T> register(type: Class<T>) {
        CapabilityManager.INSTANCE.get<T>(Type.getInternalName(type), true)
    }
}