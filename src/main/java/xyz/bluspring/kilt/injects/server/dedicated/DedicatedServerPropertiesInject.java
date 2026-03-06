package xyz.bluspring.kilt.injects.server.dedicated;

import net.minecraft.server.dedicated.DedicatedServerProperties;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DedicatedServerProperties.class)
public abstract class DedicatedServerPropertiesInject {
    // Kilt: don't need this
}
