package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.world.item.enchantment.ItemEnchantments;
import xyz.bluspring.kilt.util.KiltHelper;

public interface ItemStackInjection {
    default boolean isComponentsPatchEmpty() {
        throw new IllegalStateException();
    }
    default ItemEnchantments getTagEnchantments() {
        throw KiltHelper.createMixinException(ItemStackInjection.class, "getTagEnchantments");
    }
}
