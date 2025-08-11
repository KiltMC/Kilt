package xyz.bluspring.kilt.injects.data.tags;

import net.minecraft.data.tags.CatVariantTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CatVariantTagsProvider.class)
public abstract class CatVariantTagsProviderInject {
    // Kilt: we have no reason to implement this
}
