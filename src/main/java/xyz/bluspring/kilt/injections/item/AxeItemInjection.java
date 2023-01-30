package xyz.bluspring.kilt.injections.item;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface AxeItemInjection {
    static BlockState getAxeStrippingState(BlockState originalState) {
        var block = AxeItem.STRIPPABLES.get(originalState.getBlock());

        if (block == null)
            return null;

        return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, originalState.getValue(RotatedPillarBlock.AXIS));
    }
}
