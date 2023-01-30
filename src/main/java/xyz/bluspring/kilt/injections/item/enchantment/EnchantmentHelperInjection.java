package xyz.bluspring.kilt.injections.item.enchantment;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public interface EnchantmentHelperInjection {
    static int getTagEnchantmentLevel(Enchantment enchantment, ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
    }
}
