package xyz.bluspring.kilt.injects.world.level.block.piston;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonMovingBlockEntityInject {
    @ModifyExpressionValue(method = "moveCollidedEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    private static boolean kilt$isSlimeBlock(boolean original, @Local(argsOnly = true) PistonMovingBlockEntity movingBlockEntity) {
        return original || movingBlockEntity.getMovedState().isSlimeBlock();
    }
}
