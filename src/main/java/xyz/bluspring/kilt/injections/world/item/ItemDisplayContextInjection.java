package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.mixin.ItemDisplayContextAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

public interface ItemDisplayContextInjection {
    boolean isModded();
    @Nullable ItemDisplayContext fallback();

    void kilt$markModded();
    void kilt$setFallback(ItemDisplayContext fallback);
}
