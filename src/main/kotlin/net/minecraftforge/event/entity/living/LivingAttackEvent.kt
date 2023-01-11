package net.minecraftforge.event.entity.living

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class LivingAttackEvent(entity: LivingEntity, val source: DamageSource, val amount: Float) : LivingEvent(entity) {
}