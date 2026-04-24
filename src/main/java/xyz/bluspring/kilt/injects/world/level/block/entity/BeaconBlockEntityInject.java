package xyz.bluspring.kilt.injects.world.level.block.entity;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityInject extends BlockEntity {
    public BeaconBlockEntityInject(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Definition(id = "block", local = @Local(type = Block.class))
    @Definition(id = "BeaconBeamBlock", type = BeaconBeamBlock.class)
    @Expression("block instanceof BeaconBeamBlock")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$tryHandleBeaconColorMultiplier(boolean original, @Local Block block, @Local(ordinal = 1) BlockState state, @Local(argsOnly = true) BlockPos beaconPos, @Local(argsOnly = true) Level level, @Local(ordinal = 1) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(block.getClass(), IBlockExtension.class, "getBeaconColorMultiplier", BlockState.class, LevelReader.class, BlockPos.class, BlockPos.class)) {
            return original || state.getBeaconColorMultiplier(level, pos, beaconPos) != null;
        }

        return original;
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/DyeColor;getTextureDiffuseColor()I"))
    private static int kilt$modifyBeaconColor(int original, @Local Block block, @Local(ordinal = 1) BlockState state, @Local(argsOnly = true) BlockPos beaconPos, @Local(argsOnly = true) Level level, @Local(ordinal = 1) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(block.getClass(), IBlockExtension.class, "getBeaconColorMultiplier", BlockState.class, LevelReader.class, BlockPos.class, BlockPos.class)) {
            var multiplier = state.getBeaconColorMultiplier(level, pos, beaconPos);
            if (multiplier != null)
                return multiplier;
        }

        return original;
    }
}
