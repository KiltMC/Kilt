package xyz.bluspring.kilt.injects.world.level.block.piston;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;

@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonMovingBlockEntityInject {
    @Definition(id = "movedState", field = "Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;movedState:Lnet/minecraft/world/level/block/state/BlockState;")
    @Definition(id = "is", method = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z")
    @Definition(id = "SLIME_BLOCK", field = "Lnet/minecraft/world/level/block/Blocks;SLIME_BLOCK:Lnet/minecraft/world/level/block/Block;")
    @Expression("?.movedState.is(SLIME_BLOCK)")
    @ModifyExpressionValue(method = "moveCollidedEntities", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$isSlimeBlock(boolean original, @Local(argsOnly = true) PistonMovingBlockEntity movingBlockEntity) {
        return original || movingBlockEntity.getMovedState().isSlimeBlock();
    }
}
