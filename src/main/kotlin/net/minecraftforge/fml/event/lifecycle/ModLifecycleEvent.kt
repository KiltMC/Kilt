package net.minecraftforge.fml.event.lifecycle

import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.event.IModBusEvent
import xyz.bluspring.kilt.loader.ForgeMod

open class ModLifecycleEvent(private val mod: ForgeMod) : Event(), IModBusEvent {
    fun description(): String {
        return this.javaClass.name.run {
            this.substring(this.lastIndexOf('.') + 1)
        }
    }

    override fun toString(): String {
        return description()
    }
}