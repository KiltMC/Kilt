package thedarkcolour.kotlinforforge

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModLoadingContext
import xyz.bluspring.kilt.loader.mod.ForgeMod
import java.util.concurrent.ConcurrentHashMap

class KotlinModLoadingContext(private val mod: ForgeMod) : ModLoadingContext() {
    fun getKEventBus(): IEventBus {
        return mod.eventBus
    }

    companion object {
        private val cachedContexts = ConcurrentHashMap<String, KotlinModLoadingContext>()

        fun kiltGetContext(mod: ForgeMod): KotlinModLoadingContext {
            val ctx = cachedContexts.computeIfAbsent(mod.modId) { KotlinModLoadingContext(mod) }
            ctx.setActiveContainer(mod.container)
            return ctx
        }

        fun get(): KotlinModLoadingContext {
            return ModLoadingContext.get().extension()
        }
    }
}