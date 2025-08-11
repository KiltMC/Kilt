package xyz.bluspring.kilt.injects.world.level.block.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityInject {
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/DyeColor;getTextureDiffuseColors()[F"))
    private static float[] modifyBeaconColor(float[] original, Level level, BlockPos beaconPos, BlockState beaconState, BeaconBlockEntity beaconBlockEntity, @Local Block block, @Local(ordinal = 1) BlockState state, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(block.getClass(), Block.class, "getBeaconColorMultiplier", BlockState.class, LevelReader.class, BlockPos.class, BlockPos.class)) {
            return state.getBeaconColorMultiplier(level, pos, beaconPos);
        }

        return original;
    }
}
