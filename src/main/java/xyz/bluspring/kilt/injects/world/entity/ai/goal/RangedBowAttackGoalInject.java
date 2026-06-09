package xyz.bluspring.kilt.injects.world.entity.ai.goal;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.entity.projectile.ProjectileUtilInjection;

@Mixin(RangedBowAttackGoal.class)
public abstract class RangedBowAttackGoalInject<T extends Monster & RangedAttackMob> extends Goal {
    // Kilt: ----We don't need to implement the custom constructor stuff----
    //       (he said, like a fucking liar)

    @CreateInitializer
    public <M extends Mob & RangedAttackMob> RangedBowAttackGoalInject(M mob, double speedModifier, int attackIntervalMin, float attackRadius) {
        this((T) mob, speedModifier, attackIntervalMin, attackRadius);
    }

    public RangedBowAttackGoalInject(T mob, double speedModifier, int attackIntervalMin, float attackRadius) {}

    @Shadow @Final private T mob;

    @ModifyReturnValue(method = "isHoldingBow", at = @At("RETURN"))
    private boolean kilt$tryCheckHoldingItem(boolean original) {
        return original || this.mob.isHolding(is -> is.getItem() instanceof BowItem);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/InteractionHand;"))
    private InteractionHand kilt$tryGetWeaponHoldingHand(LivingEntity shooter, Item weapon, Operation<InteractionHand> original) {
        if (shooter.isHolding(weapon)) {
            return original.call(shooter, weapon);
        }

        return ProjectileUtilInjection.getWeaponHoldingHand(shooter, item -> item instanceof BowItem);
    }
}
