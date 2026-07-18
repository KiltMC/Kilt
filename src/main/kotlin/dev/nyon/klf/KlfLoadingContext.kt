package dev.nyon.klf

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext
import xyz.bluspring.kilt.loader.mod.ForgeMod
import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
val MOD_BUS: IEventBus
    get() {
        return KlfLoadingContext.get().getKEventBus()
    }

class KlfLoadingContext(private val mod: ForgeMod) : ModLoadingContext() {
    fun getKEventBus(): IEventBus {
        return mod.eventBus
    }

    companion object {
        private val cachedContexts = ConcurrentHashMap<String, KlfLoadingContext>()

        fun kiltGetContext(mod: ForgeMod): KlfLoadingContext {
            val ctx = cachedContexts.computeIfAbsent(mod.modId) { KlfLoadingContext(mod) }
            ctx.setActiveContainer(mod.container)
            return ctx
        }

        fun get(): KlfLoadingContext {
            return ModLoadingContext.get().extension()
        }
    }
}
