package xyz.bluspring.kilt.injections.item;

import net.minecraft.world.level.block.state.BlockState;

interface AxeItemInjection {
    default BlockState getAxeStrippingState(BlockState originalState) {
        throw new RuntimeException("mixin");
    }
}