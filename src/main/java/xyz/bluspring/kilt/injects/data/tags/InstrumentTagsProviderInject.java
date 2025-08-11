package xyz.bluspring.kilt.injects.data.tags;

import net.minecraft.data.tags.InstrumentTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(InstrumentTagsProvider.class)
public abstract class InstrumentTagsProviderInject {
    // Kilt: we have no reason to implement this
}
