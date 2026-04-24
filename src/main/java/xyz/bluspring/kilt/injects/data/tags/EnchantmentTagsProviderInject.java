package xyz.bluspring.kilt.injects.data.tags;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.data.tags.EnchantmentTagsProvider;

@Mixin(EnchantmentTagsProvider.class)
public abstract class EnchantmentTagsProviderInject {
    // Kilt: we have no reason to implement this
}
