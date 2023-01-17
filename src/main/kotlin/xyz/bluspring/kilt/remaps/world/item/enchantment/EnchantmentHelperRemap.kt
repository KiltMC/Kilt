package xyz.bluspring.kilt.remaps.world.item.enchantment

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper

object EnchantmentHelperRemap {
    @JvmStatic
    fun getTagEnchantmentLevel(enchantment: Enchantment, stack: ItemStack): Int {
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack)
    }
}