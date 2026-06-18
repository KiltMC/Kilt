package xyz.bluspring.kilt.injections.world.item;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
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
}
