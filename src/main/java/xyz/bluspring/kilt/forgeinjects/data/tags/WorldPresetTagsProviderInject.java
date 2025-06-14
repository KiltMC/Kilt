package xyz.bluspring.kilt.forgeinjects.data.tags;

import net.minecraft.data.tags.WorldPresetTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldPresetTagsProvider.class)
public abstract class WorldPresetTagsProviderInject {
    // Kilt: we have no reason to implement this
}
