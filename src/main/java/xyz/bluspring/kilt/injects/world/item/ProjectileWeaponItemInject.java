package xyz.bluspring.kilt.injects.world.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.item.ProjectileWeaponItemInjection;

import java.util.function.Predicate;

@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponItemInject implements ProjectileWeaponItemInjection {
    @Shadow public abstract Predicate<ItemStack> getSupportedHeldProjectiles();
    @Shadow public abstract Predicate<ItemStack> getAllSupportedProjectiles();

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles(ItemStack stack) {
        return this.getAllSupportedProjectiles(stack).or(this.getSupportedHeldProjectiles());
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles(ItemStack stack) {
        return this.getAllSupportedProjectiles();
    }

    @ModifyReturnValue(method = "createProjectile", at = @At("RETURN"))
    private Projectile tryGetCustomArrow(Projectile original, @Local(argsOnly = true, ordinal = 0) ItemStack weaponStack, @Local(argsOnly = true, ordinal = 1) ItemStack projectileStack) {
        if (original instanceof AbstractArrow arrow) {
            return customArrow(arrow, projectileStack, weaponStack);
        }

        return original;
    }

    @ModifyExpressionValue(method = "useAmmo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasInfiniteMaterials()Z"))
    private static boolean kilt$checkIsArrowInfinite(boolean original, @Local(argsOnly = true, ordinal = 1) ItemStack ammoStack, @Local(argsOnly = true, ordinal = 0) ItemStack weaponStack, @Local(argsOnly = true) LivingEntity entity) {
        return original || (ammoStack.getItem() instanceof ArrowItem arrowItem && arrowItem.isInfinite(ammoStack, weaponStack, entity));
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        return arrow;
    }

    @Override
    public ItemStack getDefaultCreativeAmmo(@Nullable Player player, ItemStack projectileWeaponItem) {
        return Items.ARROW.getDefaultInstance();
    }
}
