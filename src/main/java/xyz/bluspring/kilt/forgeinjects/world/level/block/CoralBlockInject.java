package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CoralBlock.class)
public class CoralBlockInject {
    @Inject(method = "scanForWater", at = @At("HEAD"))
    private void kilt$getBlockStateAtPos(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Share("state") LocalRef<BlockState> state) {
        state.set(level.getBlockState(pos));
    }

    @WrapOperation(method = "scanForWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean kilt$checkCanHydrate(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true) BlockPos pos, @Share("state") LocalRef<BlockState> state, @Local Direction direction) {
        return original.call(instance, tag) || state.get().canBeHydrated(level, pos, instance, pos.relative(direction));
    }
}
