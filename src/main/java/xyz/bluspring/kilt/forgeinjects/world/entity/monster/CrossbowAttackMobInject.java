package xyz.bluspring.kilt.forgeinjects.world.entity.monster;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.entity.ProjectileUtilInjection;

@Mixin(CrossbowAttackMob.class)
public interface CrossbowAttackMobInject {
    @WrapOperation(method = "performCrossbowAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/InteractionHand;"))
    private InteractionHand kilt$tryGetHandWithCrossbow(LivingEntity shooter, Item weapon, Operation<InteractionHand> original) {
        if (shooter.isHolding(weapon))
            return original.call(shooter, weapon);

        return ProjectileUtilInjection.getWeaponHoldingHand(shooter, item -> item instanceof CrossbowItem);
    }

    @WrapOperation(method = "performCrossbowAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isHolding(Lnet/minecraft/world/item/Item;)Z"))
    private boolean kilt$checkIsHoldingCrossbow(LivingEntity instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.isHolding(is -> is.getItem() instanceof CrossbowItem);
    }
}
