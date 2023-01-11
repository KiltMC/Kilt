package net.minecraftforge.event.entity.living

import net.minecraft.world.entity.LivingEntity
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class LivingHealEvent(entity: LivingEntity, var amount: Float) : LivingEvent(entity) {
}