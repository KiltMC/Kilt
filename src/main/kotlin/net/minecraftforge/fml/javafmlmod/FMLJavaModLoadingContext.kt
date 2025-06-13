package net.minecraftforge.fml.javafmlmod

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext
import xyz.bluspring.kilt.loader.mod.NeoForgeMod
import java.util.concurrent.ConcurrentHashMap

class FMLJavaModLoadingContext(private val mod: NeoForgeMod) : ModLoadingContext() {
    val modEventBus: IEventBus
        get() = mod.eventBus

    companion object {
        private val cachedFMLContexts = ConcurrentHashMap<String, FMLJavaModLoadingContext>()

        fun kiltGetContext(mod: NeoForgeMod): FMLJavaModLoadingContext {
            val ctx = cachedFMLContexts.computeIfAbsent(mod.modId) { FMLJavaModLoadingContext(mod) }
            ctx.setActiveContainer(mod.container)
            return ctx
        }

        @JvmStatic
        fun get(): FMLJavaModLoadingContext {
            return ModLoadingContext.get().extension()
        }
    }
}