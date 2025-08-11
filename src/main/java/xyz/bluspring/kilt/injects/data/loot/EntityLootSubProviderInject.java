package xyz.bluspring.kilt.injects.data.loot;

import net.minecraft.data.loot.EntityLootSubProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityLootSubProvider.class)
public abstract class EntityLootSubProviderInject {
    // Kilt: we have no reason to implement this
}
