package xyz.bluspring.kilt.forgeinjects.data.tags;

import net.minecraft.data.tags.PoiTypeTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PoiTypeTagsProvider.class)
public abstract class PoiTypeTagsProviderInject {
    // Kilt: we have no reason to implement this
}
