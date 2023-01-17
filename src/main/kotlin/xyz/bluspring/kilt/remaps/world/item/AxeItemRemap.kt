package xyz.bluspring.kilt.remaps.world.item

import net.minecraft.world.item.AxeItem
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState

object AxeItemRemap {
    @JvmStatic
    fun getAxeStrippingState(originalState: BlockState): BlockState? {
        val block = AxeItem.STRIPPABLES[originalState.block] ?: return null

        return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, originalState.getValue(RotatedPillarBlock.AXIS))
    }
}