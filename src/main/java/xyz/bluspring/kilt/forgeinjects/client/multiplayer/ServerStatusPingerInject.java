package xyz.bluspring.kilt.forgeinjects.client.multiplayer;

import net.minecraft.client.multiplayer.ServerStatusPinger;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerStatusPinger.class)
public abstract class ServerStatusPingerInject {
    // Kilt: we don't really have to handle this, I don't think..?
}
