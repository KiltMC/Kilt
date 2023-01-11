package net.minecraftforge.event.entity.living

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class LivingExperienceDropEvent(entity: LivingEntity, val attackingPlayer: Player?, val originalExperience: Int) : LivingEvent(entity) {
    var droppedExperience = originalExperience
}