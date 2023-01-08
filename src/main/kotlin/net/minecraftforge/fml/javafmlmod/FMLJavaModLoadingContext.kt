package net.minecraftforge.fml.javafmlmod

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext
import xyz.bluspring.kilt.loader.ForgeMod
import xyz.bluspring.kilt.loader.KiltLoader

class FMLJavaModLoadingContext {
    fun getModEventBus(): IEventBus {
        return KiltLoader.modEventBus
    }

    companion object {
        @JvmStatic
        fun get(): FMLJavaModLoadingContext {
            return ModLoadingContext.get().extension()
        }
    }
}