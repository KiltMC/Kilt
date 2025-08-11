package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SpongeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpongeBlock.class)
public abstract class SpongeBlockInject {
    @Inject(method = "removeWaterBreadthFirstSearch", at = @At("HEAD"))
    private void kilt$storeSpongeState(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Share("spongeState") LocalRef<BlockState> spongeState) {
        spongeState.set(level.getBlockState(pos));
    }

    @WrapOperation(method = "method_49829", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean kilt$checkCanSpongeBeHydrated(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true, ordinal = 0) BlockPos pos, @Local(argsOnly = true, ordinal = 1) BlockPos fluidPos) {
        return original.call(instance, tag) || level.getBlockState(pos).canBeHydrated(level, pos, instance, fluidPos);
    }
}
