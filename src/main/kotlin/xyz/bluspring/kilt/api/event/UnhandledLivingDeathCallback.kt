package xyz.bluspring.kilt.api.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity

/**
 * In some cases, NeoForge mods don't like to call [LivingEntity.die], instead they call the event directly.
 * If Kilt received the event and [LivingEntity] didn't, and you want to handle/cancel it, here you go.
 */
fun interface UnhandledLivingDeathCallback {
    fun shouldCancelUnhandledLivingDeath(entity: LivingEntity, source: DamageSource): Boolean

    companion object {
        @JvmField val EVENT: Event<UnhandledLivingDeathCallback> = EventFactory.createArrayBacked(UnhandledLivingDeathCallback::class.java) { handlers ->
            UnhandledLivingDeathCallback { entity, source ->
                for (callback in handlers) {
                    if (callback.shouldCancelUnhandledLivingDeath(entity, source))
                        return@UnhandledLivingDeathCallback true
                }

                false
            }
        }
    }
}
