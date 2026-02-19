package xyz.bluspring.kilt.injections.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import xyz.bluspring.kilt.util.KiltHelper;

public interface FireBlockInjection {
    default boolean canCatchFire(BlockGetter level, BlockPos pos, Direction face) {
        throw KiltHelper.createMixinException(FireBlockInjection.class, "canCatchFire");
    }
}
