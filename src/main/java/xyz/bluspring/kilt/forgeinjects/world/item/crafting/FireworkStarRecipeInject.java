package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.item.FireworkRocketItemShapeInjection;

@Mixin(FireworkStarRecipe.class)
public abstract class FireworkStarRecipeInject {
    @Inject(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", at = @At("TAIL"))
    private void kilt$saveShapeToTag(CraftingContainer container, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir, @Local FireworkRocketItem.Shape shape, @Local CompoundTag tag) {
        ((FireworkRocketItemShapeInjection) (Object) shape).save(tag);
    }
}
