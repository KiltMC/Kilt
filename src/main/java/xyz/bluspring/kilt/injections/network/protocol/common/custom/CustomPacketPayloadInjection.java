package xyz.bluspring.kilt.injections.network.protocol.common.custom;

import java.util.List;

import xyz.bluspring.kilt.helpers.StupidWorkarounds;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface CustomPacketPayloadInjection {
    static <B extends FriendlyByteBuf> StreamCodec<B, CustomPacketPayload> codec(
        final CustomPacketPayload.FallbackProvider<B> fallbackProvider, List<CustomPacketPayload.TypeAndCodec<? super B, ?>> payloads,
        ConnectionProtocol protocol, PacketFlow packetFlow
    ) {
        StupidWorkarounds.kilt$protocol.set(protocol);
        StupidWorkarounds.kilt$packetFlow.set(packetFlow);

        try {
            return CustomPacketPayload.codec(fallbackProvider, payloads);
        } finally {
            StupidWorkarounds.kilt$protocol.remove();
            StupidWorkarounds.kilt$packetFlow.remove();
        }
    }

    default ClientboundCustomPayloadPacket toVanillaClientbound() {
        throw KiltHelper.createMixinException(CustomPacketPayloadInjection.class, "toVanillaClientbound");
    }

    default ServerboundCustomPayloadPacket toVanillaServerbound() {
        throw KiltHelper.createMixinException(CustomPacketPayloadInjection.class, "toVanillaServerbound");
    }
}
