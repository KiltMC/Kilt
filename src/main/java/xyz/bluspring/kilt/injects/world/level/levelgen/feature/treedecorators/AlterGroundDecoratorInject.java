package xyz.bluspring.kilt.injects.world.level.levelgen.feature.treedecorators;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AlterGroundDecorator.class)
public abstract class AlterGroundDecoratorInject {
    @ModifyExpressionValue(method = "placeBlockAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider;getState(Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState kilt$callAlterGroundEvent(BlockState original, @Local(argsOnly = true) TreeDecorator.Context context, @Local(ordinal = 1) BlockPos pos) {
        return EventHooks.alterGround(context.level(), context.random(), pos, original);
    }
}
