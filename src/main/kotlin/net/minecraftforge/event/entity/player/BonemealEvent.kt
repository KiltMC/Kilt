package net.minecraftforge.event.entity.player

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.eventbus.api.Cancelable
import net.minecraftforge.eventbus.api.Event.HasResult

@Cancelable
@HasResult
class BonemealEvent(player: Player, val level: Level, val pos: BlockPos, val block: BlockState, val stack: ItemStack) : PlayerEvent(player) {
}