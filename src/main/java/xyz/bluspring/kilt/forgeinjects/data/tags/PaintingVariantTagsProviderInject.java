package xyz.bluspring.kilt.forgeinjects.data.tags;

import net.minecraft.data.tags.PaintingVariantTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PaintingVariantTagsProvider.class)
public abstract class PaintingVariantTagsProviderInject {
    // Kilt: we have no reason to implement this
}
