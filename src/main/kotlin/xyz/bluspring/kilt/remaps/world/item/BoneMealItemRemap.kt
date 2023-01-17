package xyz.bluspring.kilt.remaps.world.item

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BoneMealItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraftforge.event.ForgeEventFactory

object BoneMealItemRemap {
    @JvmField var kiltFired = false

    @JvmStatic
    fun applyBonemeal(stack: ItemStack, level: Level, pos: BlockPos, player: Player): Boolean {
        val blockState = level.getBlockState(pos)
        val hook = ForgeEventFactory.onApplyBonemeal(player, level, pos, blockState, stack)
        if (hook != 0)
            return hook > 0

        kiltFired = true
        return BoneMealItem.growCrop(stack, level, pos)
    }
}