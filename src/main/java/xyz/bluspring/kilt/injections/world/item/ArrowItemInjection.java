package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xyz.bluspring.kilt.util.KiltHelper;

public interface ArrowItemInjection {
    default boolean isInfinite(ItemStack stack, ItemStack bow, LivingEntity entity) {
        throw KiltHelper.createMixinException(ArrowItemInjection.class, "isInfinite");
    }
}
