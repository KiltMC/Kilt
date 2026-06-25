package xyz.bluspring.kilt.injects.world.entity.monster.skeleton;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.entity.projectile.ProjectileUtilInjection;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonInject extends Monster {
    protected AbstractSkeletonInject(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = {"reassessWeaponGoal", "performRangedAttack"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/InteractionHand;"))
    private InteractionHand kilt$tryGetHandWithCrossbow(LivingEntity shooter, Item weapon, Operation<InteractionHand> original) {
        if (shooter.isHolding(weapon))
            return original.call(shooter, weapon);

        return ProjectileUtilInjection.getWeaponHoldingHand(shooter, item -> item instanceof BowItem);
    }

    @ModifyExpressionValue(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/skeleton/AbstractSkeleton;getArrow(Lnet/minecraft/world/item/ItemStack;FLnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;"))
    private AbstractArrow kilt$tryUseCustomArrow(AbstractArrow original, @Local(ordinal = 1) ItemStack projectile, @Local(ordinal = 0) ItemStack weapon) {
        if (this.getMainHandItem().getItem() instanceof ProjectileWeaponItem bow) {
            return bow.customArrow(original, projectile, weapon);
        }

        return original;
    }
}
