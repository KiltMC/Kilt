package xyz.bluspring.kilt.injects.data.tags;

import net.minecraft.data.tags.StructureTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureTagsProvider.class)
public abstract class StructureTagsProviderInject {
    // Kilt: we have no reason to implement this
}
