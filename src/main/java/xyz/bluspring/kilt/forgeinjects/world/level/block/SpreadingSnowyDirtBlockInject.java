package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpreadingSnowyDirtBlock.class)
public abstract class SpreadingSnowyDirtBlockInject {
    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 0), cancellable = true)
    private void kilt$checkAreaLoaded(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!level.isAreaLoaded(pos, 1))
            ci.cancel();
    }

    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getMaxLocalRawBrightness(Lnet/minecraft/core/BlockPos;)I", ordinal = 0), cancellable = true)
    private void kilt$checkAreaLoaded2(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!level.isAreaLoaded(pos, 3))
            ci.cancel();
    }
}
