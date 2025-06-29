package thedarkcolour.kotlinforforge.neoforge

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModLoadingContext
import xyz.bluspring.kilt.loader.mod.NeoForgeMod
import java.util.concurrent.ConcurrentHashMap

class KotlinModLoadingContext(private val mod: NeoForgeMod) : ModLoadingContext() {
    fun getEventBus(): IEventBus {
        return mod.eventBus
    }

    companion object {
        private val cachedContexts = ConcurrentHashMap<String, KotlinModLoadingContext>()

        fun kiltGetContext(mod: NeoForgeMod): KotlinModLoadingContext {
            val ctx = cachedContexts.computeIfAbsent(mod.modId) { KotlinModLoadingContext(mod) }
            ctx.activeContainer = mod.container
            return ctx
        }

        /*fun get(): KotlinModLoadingContext {
            return ModLoadingContext.get().extension()
        }*/
    }
}