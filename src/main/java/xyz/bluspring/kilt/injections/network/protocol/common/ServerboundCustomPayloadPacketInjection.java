package xyz.bluspring.kilt.injections.network.protocol.common;

import com.google.common.collect.Lists;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import xyz.bluspring.kilt.injections.network.protocol.common.custom.CustomPacketPayloadInjection;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.util.Util;

public interface ServerboundCustomPayloadPacketInjection {
    StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> CONFIG_STREAM_CODEC = CustomPacketPayloadInjection.codec(
        id -> DiscardedPayload.codec(id, 32767),
        Util.make(Lists.newArrayList(new CustomPacketPayload.TypeAndCodec<>(BrandPayload.TYPE, BrandPayload.STREAM_CODEC)), $ -> {}),
        ConnectionProtocol.CONFIGURATION, PacketFlow.SERVERBOUND
    )
        .map(ServerboundCustomPayloadPacket::new, ServerboundCustomPayloadPacket::payload);

    static StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> kilt$getConfigStreamCodec(StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> original) {
        return new StreamCodec<>() {
            @Override
            public ServerboundCustomPayloadPacket decode(FriendlyByteBuf buf) {
                var currentIndex = buf.readerIndex();
                // We want to try to get at least some mod compatibility...
                var result = NetworkRegistry.getCodec(buf.readIdentifier(), ConnectionProtocol.CONFIGURATION, PacketFlow.SERVERBOUND);

                if (result != null) {
                    return new ServerboundCustomPayloadPacket(result.decode(buf));
                }

                // We failed to get this packet, fall back to the old one!
                buf.readerIndex(currentIndex);

                return original.decode(buf);
            }

            @Override
            public void encode(FriendlyByteBuf buf, ServerboundCustomPayloadPacket packet) {
                var currentIndex = buf.writerIndex();
                StreamCodec result = NetworkRegistry.getCodec(packet.type().id(), ConnectionProtocol.CONFIGURATION, PacketFlow.SERVERBOUND);

                // We want to try to get at least some mod compatibility...
                if (result != null) {
                    buf.writeIdentifier(packet.type().id());
                    result.encode(buf, packet.payload());
                    return;
                }

                // We failed to write this packet, fall back to the old one!
                buf.writerIndex(currentIndex);

                original.encode(buf, packet);
            }
        };
    }
}
