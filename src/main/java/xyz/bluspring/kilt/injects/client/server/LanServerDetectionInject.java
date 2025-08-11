package xyz.bluspring.kilt.injects.client.server;

import net.minecraft.client.server.LanServerDetection;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LanServerDetection.class)
public abstract class LanServerDetectionInject {
    // Kilt: We don't have any reason to implement IPv6 support, this should be handled by some other mod.
}
