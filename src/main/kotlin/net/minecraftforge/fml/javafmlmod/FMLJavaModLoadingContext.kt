package net.minecraftforge.fml.javafmlmod

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext
import xyz.bluspring.kilt.loader.ForgeMod

class FMLJavaModLoadingContext(private val mod: ForgeMod) {
    fun getModEventBus(): IEventBus {
        return mod.eventBus
    }

    companion object {
        @JvmStatic
        fun get(): FMLJavaModLoadingContext? {
            return ModLoadingContext.get().extension()
        }
    }
}