package xyz.bluspring.kilt.injects.data.tags;

import net.minecraft.data.tags.EntityTypeTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityTypeTagsProvider.class)
public abstract class EntityTypeTagsProviderInject {
    // Kilt: we have no reason to implement this
}
