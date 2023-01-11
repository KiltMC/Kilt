package net.minecraftforge.event.entity.living

import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraftforge.eventbus.api.Cancelable

open class MobEffectEvent(living: LivingEntity, open val effectInstance: MobEffectInstance?) : LivingEvent(living) {
    @Cancelable
    class Remove(living: LivingEntity, effectInstance: MobEffectInstance?) : LivingEvent(living) {
        val effect = effectInstance?.effect
    }

    @HasResult
    class Applicable(living: LivingEntity, override val effectInstance: MobEffectInstance) : MobEffectEvent(living, effectInstance)

    class Added(living: LivingEntity, val oldEffectInstance: MobEffectInstance, newEffectInstance: MobEffectInstance, val source: Entity) : MobEffectEvent(living, newEffectInstance)

    class Expired(living: LivingEntity, effectInstance: MobEffectInstance?) : MobEffectEvent(living, effectInstance)
}