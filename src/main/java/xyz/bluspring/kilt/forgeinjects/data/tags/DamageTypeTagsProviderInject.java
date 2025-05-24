package xyz.bluspring.kilt.forgeinjects.data.tags;

import net.minecraft.data.tags.DamageTypeTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DamageTypeTagsProvider.class)
public abstract class DamageTypeTagsProviderInject {
    // Kilt: we have no reason to implement this
}
