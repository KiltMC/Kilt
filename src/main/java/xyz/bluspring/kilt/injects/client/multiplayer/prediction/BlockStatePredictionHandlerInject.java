package xyz.bluspring.kilt.injects.client.multiplayer.prediction;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.multiplayer.prediction.BlockStatePredictionHandlerInjection;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;

@Mixin(BlockStatePredictionHandler.class)
public abstract class BlockStatePredictionHandlerInject implements BlockStatePredictionHandlerInjection {
    @Shadow
    @Final
    private Long2ObjectOpenHashMap<BlockStatePredictionHandler.ServerVerifiedState> serverVerifiedStates;

    @Inject(method = "endPredictionsUpTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;syncBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)V", shift = At.Shift.AFTER))
    private void kilt$restoreBlockEntityIfCancelled(int sequence, ClientLevel level, CallbackInfo ci, @Local BlockStatePredictionHandler.ServerVerifiedState verifiedState, @Local BlockPos pos) {
        if (verifiedState.kilt$getSnapshot() != null && verifiedState.blockState == verifiedState.kilt$getSnapshot().getState()) {
            if (verifiedState.kilt$getSnapshot().restoreBlockEntity(level, pos)) {
                level.sendBlockUpdated(pos, verifiedState.blockState, verifiedState.blockState, 3);
            }
        }
    }

    @Override
    public void retainSnapshot(BlockPos pos, BlockSnapshot snapshot) {
        this.serverVerifiedStates.get(pos.asLong()).kilt$setSnapshot(snapshot);
    }

    @Mixin(BlockStatePredictionHandler.ServerVerifiedState.class)
    public abstract static class ServerVerifiedStateInject implements ServerVerifiedStateInjection {
        @Unique BlockSnapshot snapshot;

        @Override
        public BlockSnapshot kilt$getSnapshot() {
            return this.snapshot;
        }

        @Override
        public void kilt$setSnapshot(BlockSnapshot snapshot) {
            this.snapshot = snapshot;
        }
    }
}
