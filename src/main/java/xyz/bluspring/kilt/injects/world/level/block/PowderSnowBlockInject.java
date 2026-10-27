package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.PowderSnowBlock;

@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowBlockInject {
    @WrapOperation(method = "canEntityWalkOnPowderSnow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
    private static boolean kilt$checkCanWalkOnPowderedSnow(ItemStack instance, Object o, Operation<Boolean> original, @Local(argsOnly = true) Entity entity) {
        return original.call(instance, o) || instance.canWalkOnPowderedSnow((LivingEntity) entity);
    }
}
