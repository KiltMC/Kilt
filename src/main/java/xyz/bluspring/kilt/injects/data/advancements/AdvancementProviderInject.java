package xyz.bluspring.kilt.injects.data.advancements;

import net.minecraft.data.advancements.AdvancementProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AdvancementProvider.class)
public abstract class AdvancementProviderInject {
    // Kilt: all this does is adds Deprecated.
    //       let's just bump the patch count.
}
