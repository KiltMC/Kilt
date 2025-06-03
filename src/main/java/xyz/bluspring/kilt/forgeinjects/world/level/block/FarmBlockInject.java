package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.FarmlandWaterManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Higher priority for Potatoptimize
@Mixin(value = FarmBlock.class, priority = 1050)
public abstract class FarmBlockInject {
    @Shadow public static void turnToDirt(@Nullable Entity entity, BlockState state, Level level, BlockPos pos) {}

    @Definition(id = "level", local = @Local(type = Level.class, argsOnly = true))
    @Definition(id = "random", field = "Lnet/minecraft/world/level/Level;random:Lnet/minecraft/util/RandomSource;")
    @Definition(id = "nextFloat", method = "Lnet/minecraft/util/RandomSource;nextFloat()F")
    @Definition(id = "fallDistance", local = @Local(type = float.class, argsOnly = true))
    @Expression("level.random.nextFloat() < fallDistance - 0.5")
    @ModifyExpressionValue(method = "fallOn", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$runForgeTurnToDirtCheck(boolean original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) float fallDistance, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) BlockState state) {
        // TODO: try to improve mod compatibility with this?
        if (ForgeHooks.onFarmlandTrample(level, pos, Blocks.DIRT.defaultBlockState(), fallDistance, entity)) {
            turnToDirt(entity, state, level, pos);
        }

        return false;
    }

    @ModifyReturnValue(method = "shouldMaintainFarmland", at = @At("RETURN"))
    private static boolean kilt$checkCanSustainPlant(boolean original, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true) BlockPos pos) {
        if (original)
            return true;

        var plant = level.getBlockState(pos.above());
        var state = level.getBlockState(pos);

        return plant.getBlock() instanceof IPlantable plantBlock && state.canSustainPlant(level, pos, Direction.UP, plantBlock);
    }

    @Inject(method = "isNearWater", at = @At("HEAD"))
    private static void kilt$storeBlockState(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Share("state") LocalRef<BlockState> stateRef) {
        stateRef.set(level.getBlockState(pos));
    }

    @WrapOperation(method = "isNearWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean kilt$checkCanBeHydrated(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original, @Share("state") LocalRef<BlockState> stateRef, @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true) BlockPos pos, @Local(ordinal = 1) BlockPos pos2) {
        return original.call(instance, tag) || stateRef.get().canBeHydrated(level, pos, level.getFluidState(pos2), pos2);
    }

    @ModifyReturnValue(method = "isNearWater", at = @At(value = "RETURN", ordinal = 1))
    private static boolean kilt$checkHasWaterBlockTicket(boolean original, @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true) BlockPos pos) {
        return original || FarmlandWaterManager.hasBlockWaterTicket(level, pos);
    }
}
