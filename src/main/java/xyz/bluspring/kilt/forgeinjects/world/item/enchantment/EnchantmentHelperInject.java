package xyz.bluspring.kilt.forgeinjects.world.item.enchantment;

import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.item.enchantment.EnchantmentHelperInjection;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperInject implements EnchantmentHelperInjection {
}
