package xyz.bluspring.kilt.remaps.world.item

import net.minecraft.world.item.ShovelItem
import net.minecraft.world.item.Tier
import net.minecraft.world.level.block.state.BlockState

open class ShovelItemRemap(tier: Tier, f: Float, g: Float, properties: Properties) : ShovelItem(tier, f, g, properties) {
    companion object {
        @JvmStatic
        fun getShovelPathingState(originalState: BlockState): BlockState? {
            return ShovelItem.FLATTENABLES[originalState.block]
        }
    }
}