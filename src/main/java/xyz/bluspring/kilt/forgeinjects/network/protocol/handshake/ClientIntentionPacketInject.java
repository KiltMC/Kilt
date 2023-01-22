package xyz.bluspring.kilt.forgeinjects.network.protocol.handshake;

import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.network.ClientIntentionPacketInjection;

@Mixin(ClientIntentionPacket.class)
public class ClientIntentionPacketInject implements ClientIntentionPacketInjection {
}
