// TRACKED HASH: e94105331577df75d436a2b62fbe975ba25febcf
package xyz.bluspring.kilt.injects.world.item.enchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.item.enchantment.EnchantmentHelperInjection;
import xyz.bluspring.kilt.injections.item.enchantment.EnchantmentInjection;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperInject implements EnchantmentHelperInjection {
    @CreateStatic
    private static int getTagEnchantmentLevel(Enchantment enchantment, ItemStack stack) {
        return EnchantmentHelperInjection.getTagEnchantmentLevel(enchantment, stack);
    }

    @WrapOperation(method = "getAvailableEnchantmentResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentCategory;canEnchant(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean kilt$checkCanApplyAtEnchantTable(EnchantmentCategory instance, Item item, Operation<Boolean> original, @Local Enchantment enchantment, @Local(argsOnly = true) ItemStack stack) {
        return original.call(instance, item) || ((EnchantmentInjection) enchantment).canApplyAtEnchantingTable(stack);
    }

    @ModifyVariable(method = "getAvailableEnchantmentResults", at = @At("LOAD"), ordinal = 1)
    private static boolean kilt$checkIsEnchantmentAllowedOnBooks(boolean original, @Local Enchantment enchantment) {
        return original && ((EnchantmentInjection) enchantment).isAllowedOnBooks();
    }
}