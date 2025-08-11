package xyz.bluspring.kilt.injects.world.item.enchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FrostWalkerEnchantment.class)
public abstract class FrostWalkerEnchantmentInject {
    @WrapOperation(method = "onEntityMoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isUnobstructed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Z"))
    private static boolean kilt$checkForgeBlockPlace(Level instance, BlockState blockState, BlockPos blockPos, CollisionContext collisionContext, Operation<Boolean> original, @Local(argsOnly = true) LivingEntity entity) {
        return original.call(instance, blockState, blockPos, collisionContext) && !EventHooks.onBlockPlace(entity, BlockSnapshot.create(instance.dimension(), instance, blockPos), Direction.UP);
    }
}
