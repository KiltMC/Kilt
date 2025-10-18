package xyz.bluspring.kilt.injections.network.protocol.common.custom;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;

public interface CustomPacketPayloadInjection {
    ThreadLocal<ConnectionProtocol> kilt$protocol = new ThreadLocal<>();
    ThreadLocal<PacketFlow> kilt$packetFlow = new ThreadLocal<>();

    static <B extends FriendlyByteBuf> StreamCodec<B, CustomPacketPayload> codec(
        final CustomPacketPayload.FallbackProvider<B> fallbackProvider, List<CustomPacketPayload.TypeAndCodec<? super B, ?>> payloads,
        ConnectionProtocol protocol, PacketFlow packetFlow
    ) {
        kilt$protocol.set(protocol);
        kilt$packetFlow.set(packetFlow);

        try {
            return CustomPacketPayload.codec(fallbackProvider, payloads);
        } finally {
            kilt$protocol.remove();
            kilt$packetFlow.remove();
        }
    }

    default ClientboundCustomPayloadPacket toVanillaClientbound() {
        throw KiltHelper.createMixinException(CustomPacketPayloadInjection.class, "toVanillaClientbound");
    }

    default ServerboundCustomPayloadPacket toVanillaServerbound() {
        throw KiltHelper.createMixinException(CustomPacketPayloadInjection.class, "toVanillaServerbound");
    }
}
