package xyz.bluspring.kilt.injections.world.item;

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.mixin.ItemStackAccessor;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public interface ItemStackInjection {
    default boolean isComponentsPatchEmpty() {
        throw new IllegalStateException();
    }

    default void hurtAndBreak(int damage, ServerLevel level, @Nullable LivingEntity entity, Consumer<Item> onBreak) {
        throw KiltHelper.createMixinException(ItemStackInjection.class, "hurtAndBreak");
    }

    default ItemEnchantments getTagEnchantments() {
        throw KiltHelper.createMixinException(ItemStackInjection.class, "getTagEnchantments");
    }

    static boolean isSameItem(ItemStack a, @Nullable ItemStackTemplate b) {
        return b == null ? a.isEmpty() : a.is(b.item());
    }

    static boolean isSameItemSameComponents(ItemStack a, @Nullable ItemStackTemplate b) {
        if (a.isEmpty() || b == null) {
            return a.isEmpty() == (b == null);
        } else {
            return a.is(b.item()) && ((ItemStackAccessor) (Object) a).kilt$getComponents().patchEquals(b.components());
        }
    }
}
