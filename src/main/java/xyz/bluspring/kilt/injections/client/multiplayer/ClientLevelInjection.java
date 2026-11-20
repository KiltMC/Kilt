package xyz.bluspring.kilt.injections.client.multiplayer;

import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.HitResult;

public interface ClientLevelInjection {
    default void addBreakingBlockEffect(BlockPos pos, Direction direction, @Nullable HitResult hitResult) {
        throw KiltHelper.createMixinException(ClientLevelInjection.class, "addBreakingBlockEffect");
    }
}
