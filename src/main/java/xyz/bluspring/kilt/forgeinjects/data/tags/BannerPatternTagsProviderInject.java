package xyz.bluspring.kilt.forgeinjects.data.tags;

import net.minecraft.data.tags.BannerPatternTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BannerPatternTagsProvider.class)
public abstract class BannerPatternTagsProviderInject {
    // Kilt: we have no reason to implement this
}
