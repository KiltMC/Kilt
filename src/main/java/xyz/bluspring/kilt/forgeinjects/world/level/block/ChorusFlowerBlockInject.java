package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChorusFlowerBlock.class)
public abstract class ChorusFlowerBlockInject {
    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0), cancellable = true)
    private void kilt$preGrowEvent(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Local(ordinal = 1) BlockPos pos2, @Share("shouldRunPostEvent") LocalBooleanRef shouldRunPostEvent) {
        if (!ForgeHooks.onCropsGrowPre(level, pos2, state, true)) {
            ci.cancel();
        }

        shouldRunPostEvent.set(true);
    }

    @Inject(method = "randomTick", at = @At("RETURN"))
    private void kilt$postGrowEvent(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("shouldRunPostEvent") LocalBooleanRef shouldRunPostEvent) {
        if (shouldRunPostEvent.get()) {
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }
}
