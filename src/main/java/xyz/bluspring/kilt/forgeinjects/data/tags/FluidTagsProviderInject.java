package xyz.bluspring.kilt.forgeinjects.data.tags;

import net.minecraft.data.tags.FluidTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FluidTagsProvider.class)
public abstract class FluidTagsProviderInject {
    // Kilt: we have no reason to implement this
}
