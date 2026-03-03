package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    // Kilt TODO: a bit more left but need to head out

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        return arrow;
    }

    @Override
    public ItemStack getDefaultCreativeAmmo(@Nullable Player player, ItemStack projectileWeaponItem) {
        return Items.ARROW.getDefaultInstance();
    }
}
