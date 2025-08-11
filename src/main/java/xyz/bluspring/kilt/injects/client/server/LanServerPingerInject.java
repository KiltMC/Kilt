package xyz.bluspring.kilt.injects.client.server;

import net.minecraft.client.server.LanServerPinger;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LanServerPinger.class)
public abstract class LanServerPingerInject {
    // Kilt: We don't have any reason to implement IPv6 support, this should be handled by some other mod.
}
