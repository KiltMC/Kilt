// TRACKED HASH: d335200a23b4a1e738aa9a7ea4e390f5f72a6c5d
package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BushBlock.class, priority = 950)
public abstract class BushBlockInject extends Block implements IPlantable {
    public BushBlockInject(Properties properties) {
        super(properties);
    }

    @WrapOperation(
            method = "canSurvive",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BushBlock;mayPlaceOn(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z")
    )
    private boolean kilt$checkCanSustain(
            BushBlock instance, BlockState stateAtPos, BlockGetter level, BlockPos posBelow, Operation<Boolean> original,
            @Local(argsOnly = true) BlockState state
    ) {
        if (state.getBlock() == this) {
            return stateAtPos.canSustainPlant(level, posBelow, Direction.UP, this);
        }
        return original.call(instance, stateAtPos, level, posBelow);
    }

    // Kilt: handled by Porting Lib
    /*@Override
    public BlockState getPlant(BlockGetter level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.getBlock() != this)
            return this.defaultBlockState();

        return state;
    }*/
}