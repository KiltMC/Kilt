package xyz.bluspring.kilt.injections.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

public interface FireBlockInjection {
    boolean canCatchFire(BlockGetter level, BlockPos pos, Direction face);
}
