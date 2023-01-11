package net.minecraftforge.event.entity.living

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraftforge.event.entity.EntityEvent
import net.minecraftforge.eventbus.api.Cancelable

open class LivingEvent(livingEntity: LivingEntity) : EntityEvent(livingEntity) {
    override val entity = livingEntity

    @Cancelable
    open class LivingTickEvent(e: LivingEntity) : LivingEvent(e)

    open class LivingJumpEvent(e: LivingEntity) : LivingEvent(e)

    open class LivingVisibilityEvent(livingEntity: LivingEntity, val lookingEntity: Entity?, originalMultiplier: Double) : LivingEvent(livingEntity) {
        var visibilityModifier: Double = originalMultiplier
            private set

        fun modifyVisibility(mod: Double) {
            visibilityModifier *= mod
        }
    }
}