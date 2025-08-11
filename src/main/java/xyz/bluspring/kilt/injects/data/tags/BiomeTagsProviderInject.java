package xyz.bluspring.kilt.injects.data.tags;

import net.minecraft.data.tags.BiomeTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BiomeTagsProvider.class)
public abstract class BiomeTagsProviderInject {
    // Kilt: we have no reason to implement this
}
