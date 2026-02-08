package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.mixin.ItemDisplayContextAccessor;
import xyz.bluspring.kilt.util.EnumUtils;
import xyz.bluspring.kilt.util.KiltHelper;

public interface ItemDisplayContextInjection {
    default boolean isModded() {
        return false;
    }

    default @Nullable ItemDisplayContext fallback() {
        throw KiltHelper.createMixinException(ItemDisplayContextInjection.class, "fallback");
    }

    default void kilt$markModded() {
        throw KiltHelper.createMixinException(ItemDisplayContextInjection.class, "kilt$markModded");
    }

    default void kilt$setFallback(ItemDisplayContext fallback) {
        throw KiltHelper.createMixinException(ItemDisplayContextInjection.class, "kilt$setFallback");
    }
}
