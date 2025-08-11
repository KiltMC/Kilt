package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChorusPlantBlock.class)
public abstract class ChorusPlantBlockInject {
    @WrapOperation(method = {"getStateForPlacement(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", "updateShape", "canSurvive"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 2))
    private boolean kilt$detectForgeChorusTag(BlockState instance, Block block, Operation<Boolean> original) {
        return original.call(instance, block) || instance.is(Tags.Blocks.CHORUS_ADDITIONALLY_GROWS_ON);
    }

    @WrapOperation(method = "canSurvive", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 4))
    private boolean kilt$checkChorusGrowsOn(BlockState instance, Block block, Operation<Boolean> original) {
        return original.call(instance, block) || instance.is(Tags.Blocks.CHORUS_ADDITIONALLY_GROWS_ON);
    }
}
