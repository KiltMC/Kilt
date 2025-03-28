package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.item.ArrowItemInjection;

@Mixin(ArrowItem.class)
public abstract class ArrowItemInject implements ArrowItemInjection {
    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, Player player) {
        int enchant = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow);
        return enchant <= 0 ? false : ((Object) this).getClass() == ArrowItem.class;
    }
}
