package xyz.bluspring.kilt.injects.world.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(ExplosionDamageCalculator.class)
public abstract class ExplosionDamageCalculatorInject {
    @WrapOperation(method = "getBlockExplosionResistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getExplosionResistance()F"))
    private float kilt$tryUseForgeExplosionResistance(Block instance, Operation<Float> original, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true) Explosion explosion, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Block.class, "getExplosionResistance", BlockState.class, BlockGetter.class, BlockPos.class, Explosion.class)) {
            return instance.getExplosionResistance(state, level, pos, explosion);
        }

        return original.call(instance);
    }

    @WrapOperation(method = "getBlockExplosionResistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getExplosionResistance()F"))
    private float kilt$tryUseForgeExplosionResistance(FluidState instance, Operation<Float> original, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true) Explosion explosion, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getType().getClass(), Fluid.class, "getExplosionResistance", FluidState.class, BlockGetter.class, BlockPos.class, Explosion.class)) {
            return instance.getExplosionResistance(level, pos, explosion);
        }

        return original.call(instance);
    }
}
