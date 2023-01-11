package net.minecraftforge.event.entity.living

import net.minecraft.world.entity.LivingEntity

class LivingFallEvent(entity: LivingEntity, var distance: Float, var damageMultiplier: Float) : LivingEvent(entity) {
}