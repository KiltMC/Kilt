package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.common.util.TriState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChorusPlantBlock.class)
public abstract class ChorusPlantBlockInject {
    @WrapOperation(method = "getStateWithConnections", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;trySetValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;", ordinal = 0))
    private static <T extends Comparable<T>, V extends T> Object canSustain(BlockState instance, Property<T> property, Comparable<Boolean> comparable, Operation<Object> original, BlockGetter level, BlockPos pos, BlockState state, @Local(ordinal = 0) BlockState plantState) {
        TriState canSustain = plantState.canSustainPlant(level, pos.below(), Direction.UP, instance);
        return original.call(instance, property, ((Boolean) comparable) || canSustain.isTrue());
    }

    @Inject(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"))
    private void canSustain(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir, @Local LocalBooleanRef canSustainFlag) {
        if (direction == Direction.DOWN) {
            TriState soilDecision = neighborState.canSustainPlant(level, neighborPos.relative(direction), direction.getOpposite(), state);
            if (!soilDecision.isDefault()) {
                canSustainFlag.set(soilDecision.isTrue());
            }
        }
    }

    @ModifyReturnValue(method = "canSurvive", at = @At(value = "RETURN", ordinal = 2))
    private boolean checkChorusGrowsOn(boolean original, BlockState state, LevelReader level, BlockPos pos, @Local(ordinal = 0) BlockState soilState) {
        TriState soilDecision = soilState.canSustainPlant(level, pos.below(), Direction.UP, state);
        if (!soilDecision.isDefault()) return soilDecision.isTrue();
        return original;
    }
}
