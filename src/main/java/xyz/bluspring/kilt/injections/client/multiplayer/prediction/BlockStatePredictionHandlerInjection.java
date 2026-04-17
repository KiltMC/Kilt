package xyz.bluspring.kilt.injections.client.multiplayer.prediction;

import net.neoforged.neoforge.common.util.BlockSnapshot;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;

public interface BlockStatePredictionHandlerInjection {
    default void retainSnapshot(BlockPos pos, BlockSnapshot snapshot) {
        throw KiltHelper.createMixinException(BlockStatePredictionHandlerInjection.class, "retainSnapshot");
    }

    interface ServerVerifiedStateInjection {
        default BlockSnapshot kilt$getSnapshot() {
            throw KiltHelper.createMixinException(BlockStatePredictionHandlerInjection.class, "kilt$getSnapshot");
        }

        default void kilt$setSnapshot(BlockSnapshot snapshot) {
            throw KiltHelper.createMixinException(BlockStatePredictionHandlerInjection.class, "kilt$setSnapshot");
        }
    }
}
