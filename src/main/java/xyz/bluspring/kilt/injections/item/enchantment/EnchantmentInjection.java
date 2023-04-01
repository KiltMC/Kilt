package xyz.bluspring.kilt.injections.item.enchantment;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantmentInjection {
    default boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.canApplyAtEnchantingTable((Enchantment) this);
    }

    default boolean isAllowedOnBooks() {
        return true;
    }
}
