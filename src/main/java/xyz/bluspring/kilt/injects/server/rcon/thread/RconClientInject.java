package xyz.bluspring.kilt.injects.server.rcon.thread;

import net.minecraft.server.rcon.thread.RconClient;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RconClient.class)
public abstract class RconClientInject {
    // Kilt: I'm not touching this
}
