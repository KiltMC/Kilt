package xyz.bluspring.kilt.helpers;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;

public interface StupidWorkarounds {
    // for CustomPacketPayloadInjection - because CreativeCore just doesn't work properly otherwise, due to reflection bullshit. it's dumb.
    ThreadLocal<ConnectionProtocol> kilt$protocol = new ThreadLocal<>();
    ThreadLocal<PacketFlow> kilt$packetFlow = new ThreadLocal<>();
}
