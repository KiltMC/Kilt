package net.minecraftforge.fml.javafmlmod

import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.event.IModBusEvent
import org.apache.logging.log4j.LogManager
import xyz.bluspring.kilt.loader.ForgeMod
import xyz.bluspring.kilt.loader.KiltLoader

class FMLJavaModLoadingContext(private val mod: ForgeMod) {
    val modEventBus: IEventBus
        get() = mod.eventBus

    companion object {
        @JvmStatic
        fun get(): FMLJavaModLoadingContext {
            return ModLoadingContext.get().extension()
        }
    }
}