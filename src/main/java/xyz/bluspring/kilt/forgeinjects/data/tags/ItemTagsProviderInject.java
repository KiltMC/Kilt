package xyz.bluspring.kilt.forgeinjects.data.tags;

import net.minecraft.data.tags.ItemTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemTagsProvider.class)
public abstract class ItemTagsProviderInject {
    // Kilt: we have no reason to implement this
}
