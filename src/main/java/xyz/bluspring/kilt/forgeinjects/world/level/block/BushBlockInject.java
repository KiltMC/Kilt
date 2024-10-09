package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BushBlock.class)
public abstract class BushBlockInject implements IPlantable {
    @Inject(method = "canSurvive", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BushBlock;mayPlaceOn(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"), cancellable = true)
    private void kilt$checkCanSustain(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 1) BlockPos pos2) {
        if (state.getBlock() == (Object) this) {
            cir.setReturnValue(level.getBlockState(pos2).canSustainPlant(level, pos2, Direction.UP, this));
        }
    }
}
