package xyz.bluspring.kilt.mixin.workarounds;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
}
