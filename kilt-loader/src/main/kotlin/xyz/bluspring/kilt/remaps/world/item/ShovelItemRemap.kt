package xyz.bluspring.kilt.remaps.world.item

import net.minecraft.world.item.ShovelItem
import net.minecraft.world.level.block.state.BlockState

object ShovelItemRemap {
    @JvmStatic
    fun getShovelPathingState(originalState: BlockState): BlockState? {
        return ShovelItem.FLATTENABLES[originalState.block]
    }
}