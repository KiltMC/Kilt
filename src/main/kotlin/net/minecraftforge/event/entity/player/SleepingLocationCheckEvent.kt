package net.minecraftforge.event.entity.player

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.eventbus.api.Event.HasResult

@HasResult
class SleepingLocationCheckEvent(player: LivingEntity, val sleepingLocation: BlockPos) : LivingEvent(player) {
}