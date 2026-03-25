package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RepairItemRecipe;

import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(RepairItemRecipe.class)
public abstract class RepairItemRecipeInject {
    @ModifyExpressionValue(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;canBeDepleted()Z"))
    private boolean kilt$checkRepairable(boolean original, @Local(index = 6) ItemStack stack) {
        return original || stack.isRepairable();
    }

    @ModifyExpressionValue(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;canBeDepleted()Z", ordinal = 0))
    private boolean kilt$checkRepairable2(boolean original, @Local(index = 6) ItemStack stack) {
        return original || stack.isRepairable();
    }

    @ModifyExpressionValue(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;canBeDepleted()Z", ordinal = 1))
    private boolean kilt$checkRepairable3(boolean original, @Local(index = 4) ItemStack stack) {
        return original || stack.isRepairable();
    }

    @WrapOperation(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getMaxDamage()I"))
    private int kilt$tryGetMaxDamage(Item instance, Operation<Integer> original, @Local(ordinal = 0) ItemStack stack) {
        if (KiltHelper.INSTANCE.hasMethodOverride(stack.getItem().getClass(), IForgeItem.class, "getMaxDamage", ItemStack.class)) {
            return stack.getMaxDamage();
        }

        return original.call(instance);
    }
}
