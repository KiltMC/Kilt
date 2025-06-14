package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SaplingBlock.class)
public abstract class SaplingBlockInject {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void kilt$checkAreaLoaded(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!level.isAreaLoaded(pos, 1))
            ci.cancel();
    }
}
