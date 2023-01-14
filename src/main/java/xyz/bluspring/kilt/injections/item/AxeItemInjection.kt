package xyz.bluspring.kilt.injections.item

import net.minecraft.world.level.block.state.BlockState

interface AxeItemInjection {
    fun getAxeStrippingState(originalState: BlockState): BlockState
}