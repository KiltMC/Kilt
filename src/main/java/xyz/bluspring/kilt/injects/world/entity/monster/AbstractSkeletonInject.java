package xyz.bluspring.kilt.injects.world.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.entity.ProjectileUtilInjection;
import xyz.bluspring.kilt.injections.world.item.BowItemInjection;

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

    @ModifyExpressionValue(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/AbstractSkeleton;getArrow(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/projectile/AbstractArrow;"))
    private AbstractArrow kilt$tryUseCustomArrow(AbstractArrow original) {
        if (this.getMainHandItem().getItem() instanceof BowItem bow) {
            return ((BowItemInjection) bow).customArrow(original);
        }

        return original;
    }
}
