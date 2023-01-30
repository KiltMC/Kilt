package xyz.bluspring.kilt.injections.item;

import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.state.BlockState;

public interface ShovelItemInjection {
    static BlockState getShovelPathingState(BlockState originalState) {
        return ShovelItem.FLATTENABLES.get(originalState.getBlock());
    }
}
