package xyz.bluspring.kilt.forgeinjects.data.tags;

import net.minecraft.data.tags.StructureTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureTagsProvider.class)
public abstract class StructureTagsProviderInject {
    // Kilt: we have no reason to implement this
}
