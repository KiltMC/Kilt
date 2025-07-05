package dev.engine_room.flywheel.api.event

import net.minecraft.client.Minecraft
import net.minecraft.server.packs.resources.ResourceManager
import net.neoforged.bus.api.Event
import net.neoforged.fml.event.IModBusEvent
import java.util.*

class EndClientResourceReloadEvent(private val minecraft: Minecraft?, private val resourceManager: ResourceManager?, private val initialReload: Boolean, private val error: Optional<Throwable>) : Event(), IModBusEvent {
    fun minecraft(): Minecraft {
        return this.minecraft!!
    }

    fun resourceManager(): ResourceManager {
        return this.resourceManager!!
    }

    fun isInitialReload(): Boolean {
        return this.initialReload
    }

    fun error(): Optional<Throwable> {
        return this.error
    }

    constructor() : this(null, null, false, Optional.empty())
}