package xyz.bluspring.kilt.remaps.world.item

import net.minecraft.world.item.AxeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Tier
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState

open class AxeItemRemap(tier: Tier, f: Float, g: Float, properties: Item.Properties) : AxeItem(tier, f, g, properties) {
    companion object {
        @JvmStatic
        fun getAxeStrippingState(originalState: BlockState): BlockState? {
            val block = AxeItem.STRIPPABLES[originalState.block] ?: return null

            return block.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, originalState.getValue(RotatedPillarBlock.AXIS))
        }
    }
}