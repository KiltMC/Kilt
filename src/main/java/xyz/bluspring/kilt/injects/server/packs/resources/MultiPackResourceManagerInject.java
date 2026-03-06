package xyz.bluspring.kilt.injects.server.packs.resources;

import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MultiPackResourceManager.class)
public abstract class MultiPackResourceManagerInject {
    // Kilt: don't think we need this
}
