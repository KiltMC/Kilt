package xyz.bluspring.kilt.helpers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;

public interface StupidWorkarounds {
    // for CustomPacketPayloadInjection - because CreativeCore just doesn't work properly otherwise, due to reflection bullshit. it's dumb.
    ThreadLocal<ConnectionProtocol> kilt$protocol = new ThreadLocal<>();
    ThreadLocal<PacketFlow> kilt$packetFlow = new ThreadLocal<>();

    Map<FluidType, IClientFluidTypeExtensions> kilt$fabricFluidExtensions = Collections.synchronizedMap(new HashMap<>());
}
