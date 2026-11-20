package xyz.bluspring.kilt.injects.world.level.lighting;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

@Mixin(LightEngine.class)
public abstract class LightEngineInject {
    @Shadow
    public static boolean hasDifferentLightProperties(BlockState oldState, BlockState newState) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Unique private static final ThreadLocal<BlockGetter> kilt$level = ThreadLocal.withInitial(() -> EmptyBlockGetter.INSTANCE);
    @Unique private static final ThreadLocal<BlockPos> kilt$pos = ThreadLocal.withInitial(() -> BlockPos.ZERO);

    @CreateStatic
    private static boolean hasDifferentLightProperties(BlockGetter level, BlockPos pos, BlockState oldState, BlockState newState) {
        try {
            kilt$level.set(level);
            kilt$pos.set(pos);
            return hasDifferentLightProperties(oldState, newState);
        } finally {
            kilt$level.remove();
            kilt$pos.remove();
        }
    }

    @Definition(id = "getLightEmission", method = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I")
    @Expression("?.getLightEmission()")
    @WrapOperation(method = "hasDifferentLightProperties", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static int kilt$useForgeLightEmission(BlockState instance, Operation<Integer> original) {
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(instance.getBlock().getClass(), IBlockExtension.class, "getLightEmission", int.class, BlockState.class, BlockGetter.class, BlockPos.class)) {
            var level = kilt$level.get();
            var pos = kilt$pos.get();
            return instance.getLightEmission(level, pos);
        }

        return original.call(instance);
    }
}
