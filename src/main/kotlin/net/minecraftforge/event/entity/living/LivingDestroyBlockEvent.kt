package net.minecraftforge.event.entity.living

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class LivingDestroyBlockEvent(entity: LivingEntity, val pos: BlockPos, val state: BlockState) : LivingEvent(entity) {
}