package xyz.bluspring.kilt.injects.world.level.pathfinder;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.level.pathfinder.PathfindingContextInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorInject {
    @WrapOperation(method = "checkNeighbourBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/PathfindingContext;getPathTypeFromState(III)Lnet/minecraft/world/level/pathfinder/PathType;"), cancellable = true)
    private static PathType kilt$tryGetAdjacentBlockPathType(PathfindingContext instance, int x, int y, int z, Operation<PathType> original, @Cancellable CallbackInfoReturnable<PathType> cir) {
        PathType pathType = original.call(instance, x, y, z);
        BlockPos currentEvalPos = ((PathfindingContextInjection) instance).currentEvalPos();
        BlockState blockState = instance.level().getBlockState(currentEvalPos);

        if (KiltHelper.INSTANCE.hasMethodOverride(blockState.getBlock().getClass(), Block.class, "getAdjacentBlockPathType", BlockState.class, BlockGetter.class, BlockPos.class, Mob.class, PathType.class)) {
            PathType blockPathType = blockState.getAdjacentBlockPathType(instance.level(), currentEvalPos, null, pathType);
            if (blockPathType != null) {
                cir.setReturnValue(blockPathType);
                return pathType;
            }
        }

        FluidState fluidState = blockState.getFluidState();
        PathType fluidPathType = fluidState.getAdjacentBlockPathType(instance.level(), currentEvalPos, null, pathType);
        if (fluidPathType != null) { // This replaces vanilla, should probably add a check for kilt stuff or something
            cir.setReturnValue(fluidPathType);
        }

        return pathType;
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
