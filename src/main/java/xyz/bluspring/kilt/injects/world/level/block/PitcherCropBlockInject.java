package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.level.block.CropBlockInjection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(PitcherCropBlock.class)
public abstract class PitcherCropBlockInject extends DoublePlantBlock {
    public PitcherCropBlockInject(Properties properties) {
        super(properties);
    }

    @Expression("false")
    @ModifyExpressionValue(method = "canSurvive", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkSoilDecisionWorks(boolean original, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) LevelReader level, @Local(argsOnly = true) BlockPos pos) {
        var soilDecision = level.getBlockState(pos.below()).canSustainPlant(level, pos.below(), Direction.UP, state);
        return soilDecision.toBoolean(original);
    }

    @ModifyReturnValue(method = "mayPlaceOn", at = @At("RETURN"))
    private boolean kilt$checkIsFarmBlock(boolean original, @Local(argsOnly = true) BlockState state) {
        return original;
    }

    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/CropBlock;getGrowthSpeed(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    private float kilt$addStateToSpeedHandling(Block block, BlockGetter level, BlockPos pos, Operation<Float> original, @Local(argsOnly = true) BlockState state) {
        try {
            CropBlockInjection.kilt$currentState.set(state);
            return original.call(block, level, pos);
        } finally {
            CropBlockInjection.kilt$currentState.remove();
        }
    }
}
