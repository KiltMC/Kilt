package net.minecraftforge.fml.event.lifecycle

import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.InterModComms
import net.minecraftforge.fml.event.IModBusEvent
import xyz.bluspring.kilt.loader.ForgeMod
import java.util.function.Predicate
import java.util.stream.Stream

open class ModLifecycleEvent(private val mod: ForgeMod?) : Event(), IModBusEvent {
    constructor() : this(null)

    fun description(): String {
        return this.javaClass.name.run {
            this.substring(this.lastIndexOf('.') + 1)
        }
    }

    val IMCStream: Stream<InterModComms.IMCMessage>
        get() = InterModComms.getMessages(mod!!.modInfo.mod.modId)

    fun getIMCStream(methodFilter: Predicate<String>): Stream<InterModComms.IMCMessage> {
        return InterModComms.getMessages(mod!!.modInfo.mod.modId, methodFilter)
    }

    override fun toString(): String {
        return description()
    }
}