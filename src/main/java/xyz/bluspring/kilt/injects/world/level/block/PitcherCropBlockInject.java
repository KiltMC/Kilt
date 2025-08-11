package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.IPlantable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PitcherCropBlock.class)
public abstract class PitcherCropBlockInject extends DoublePlantBlock {
    public PitcherCropBlockInject(Properties properties) {
        super(properties);
    }

    @ModifyExpressionValue(method = "canSurvive", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/PitcherCropBlock;mayPlaceOn(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kilt$ensureCanSustainInWorldGen(boolean original, BlockState state, LevelReader level, BlockPos pos) {
        if (state.getBlock() == this) {
            return level.getBlockState(pos.below()).canSustainPlant(level, pos.below(), Direction.UP, (IPlantable) this);
        }

        return original;
    }
}
