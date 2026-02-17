package xyz.bluspring.kilt.workarounds;

import net.minecraft.network.protocol.Packet;

public interface ICommonPacketListenerWorkaround {
    void send(Packet<?> packet);
}
