package xyz.bluspring.kilt.forgeinjects.world.entity.projectile;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.entity.ProjectileUtilInjection;

import java.util.function.Predicate;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilInject implements ProjectileUtilInjection {
    @CreateStatic
    private static InteractionHand getWeaponHoldingHand(LivingEntity livingEntity, Predicate<Item> itemPredicate) {
        return ProjectileUtilInjection.getWeaponHoldingHand(livingEntity, itemPredicate);
    }
}
