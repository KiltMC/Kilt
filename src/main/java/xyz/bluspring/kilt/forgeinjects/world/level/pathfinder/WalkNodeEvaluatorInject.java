package xyz.bluspring.kilt.forgeinjects.world.level.pathfinder;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorInject {
    @WrapOperation(method = {"getNeighbors", "getMobJumpHeight"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;maxUpStep()F"))
    private float kilt$useStepHeight(Mob instance, Operation<Float> original) {
        return Math.max(instance.getStepHeight(), original.call(instance)); // Kilt: use whatever is highest honestly
    }

    @Inject(method = "checkNeighbourBlocks", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", shift = At.Shift.AFTER), cancellable = true)
    private static void kilt$tryGetAdjacentBlockPathType(BlockGetter level, BlockPos.MutableBlockPos centerPos, BlockPathTypes nodeType, CallbackInfoReturnable<BlockPathTypes> cir, @Local BlockState state) {
        try {
            var blockPathType = state.getAdjacentBlockPathType(level, centerPos, null, nodeType);
            if (blockPathType != null)
                cir.setReturnValue(blockPathType);

            var fluidState = state.getFluidState();
            var fluidPathType = fluidState.getAdjacentBlockPathType(level, centerPos, null, nodeType);
            if (fluidPathType != null)
                cir.setReturnValue(fluidPathType);
        } catch (NullPointerException ignored) {} // Kilt: The Forge deferred registry is going to be the fucking death of me. (crash only triggers w/ Lithium)
    }

    @Inject(method = "getBlockPathTypeRaw", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", shift = At.Shift.AFTER), cancellable = true)
    private static void kilt$tryGetBlockPathType(BlockGetter level, BlockPos pos, CallbackInfoReturnable<BlockPathTypes> cir, @Local BlockState state) {
        try {
            var type = state.getBlockPathType(level, pos, null);
            if (type != null)
                cir.setReturnValue(type);
        } catch (NullPointerException ignored) {} // Kilt: The Forge deferred registry is going to be the fucking death of me. (crash only triggers w/ Lithium)
    }

    @Inject(method = "getBlockPathTypeRaw", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/BlockGetter;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;", shift = At.Shift.AFTER), cancellable = true)
    private static void kilt$tryGetFluidBlockPathType(BlockGetter level, BlockPos pos, CallbackInfoReturnable<BlockPathTypes> cir, @Local FluidState state) {
        try {
            var type = state.getBlockPathType(level, pos, null, false);
            if (type != null)
                cir.setReturnValue(type);
        } catch (NullPointerException ignored) {} // Kilt: The Forge deferred registry is going to be the fucking death of me. (crash only triggers w/ Lithium)
    }

    @Inject(method = "getBlockPathTypeRaw", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 1), cancellable = true)
    private static void kilt$tryGetLoggableFluidBlockPathType(BlockGetter level, BlockPos pos, CallbackInfoReturnable<BlockPathTypes> cir, @Local FluidState state) {
        try {
            var type = state.getBlockPathType(level, pos, null, true);
            if (type != null)
                cir.setReturnValue(type);
        } catch (NullPointerException ignored) {} // Kilt: The Forge deferred registry is going to be the fucking death of me. (crash only triggers w/ Lithium)
    }
}
