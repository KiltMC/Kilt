package net.minecraftforge.fml.javafmlmod

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext
import xyz.bluspring.kilt.loader.mod.ForgeMod
import java.util.concurrent.ConcurrentHashMap

class FMLJavaModLoadingContext(private val mod: ForgeMod) : ModLoadingContext() {
    val modEventBus: IEventBus
        get() = mod.eventBus

    companion object {
        private val cachedFMLContexts = ConcurrentHashMap<String, FMLJavaModLoadingContext>()

        fun kiltGetContext(mod: ForgeMod): FMLJavaModLoadingContext {
            return cachedFMLContexts.computeIfAbsent(mod.modId) { FMLJavaModLoadingContext(mod) }
        }

        @JvmStatic
        fun get(): FMLJavaModLoadingContext {
            return ModLoadingContext.get().extension()
        }
    }
}