package net.minecraftforge.fml.javafmlmod

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext
import xyz.bluspring.kilt.loader.mod.ForgeMod

class FMLJavaModLoadingContext(mod: ForgeMod) : ModLoadingContext(mod) {
    val modEventBus: IEventBus
        get() = mod.eventBus

    companion object {
        @JvmStatic
        fun get(): FMLJavaModLoadingContext {
            return ModLoadingContext.get().extension()
        }
    }
}