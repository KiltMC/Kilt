package xyz.bluspring.kilt.forgeinjects.world.level.lighting;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightEngine.class)
public abstract class LightEngineInject {
    @Definition(id = "state2", local = @Local(type = BlockState.class, ordinal = 1, argsOnly = true))
    @Definition(id = "getLightEmission", method = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I")
    @Definition(id = "state1", local = @Local(type = BlockState.class, ordinal = 0, argsOnly = true))
    @Expression("state2.getLightEmission() == state1.getLightEmission()")
    @ModifyExpressionValue(method = "hasDifferentLightProperties", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$useForgeLightEmissionTest(boolean original, BlockGetter level, BlockPos pos, BlockState state1, BlockState state2) {
        return state2.getLightEmission(level, pos) == state1.getLightEmission(level, pos);
    }
}
