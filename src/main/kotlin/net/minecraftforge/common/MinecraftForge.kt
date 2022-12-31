package net.minecraftforge.common

import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.IEventBus

object MinecraftForge {
    @JvmStatic
    val EVENT_BUS: IEventBus = BusBuilder.builder().apply {
        startShutdown()
    }.build()

    @JvmStatic
    fun initialize() {

    }
}