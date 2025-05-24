package xyz.bluspring.kilt.forgeinjects.server.packs.resources;

import net.minecraft.server.packs.resources.FallbackResourceManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FallbackResourceManager.class)
public abstract class FallbackResourceManagerInject {
    // Kilt: don't think we need to implement this
}
