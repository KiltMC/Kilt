package xyz.bluspring.kilt.injections.network.protocol.common;

import com.google.common.collect.Lists;
import net.minecraft.Util;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import xyz.bluspring.kilt.injections.network.protocol.common.custom.CustomPacketPayloadInjection;

public interface ServerboundCustomPayloadPacketInjection {
    StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> CONFIG_STREAM_CODEC = CustomPacketPayloadInjection.<FriendlyByteBuf>codec(
        id -> DiscardedPayload.codec(id, 32767),
        Util.make(Lists.newArrayList(new CustomPacketPayload.TypeAndCodec<>(BrandPayload.TYPE, BrandPayload.STREAM_CODEC)), $ -> {}),
        ConnectionProtocol.CONFIGURATION, PacketFlow.SERVERBOUND
    )
        .map(ServerboundCustomPayloadPacket::new, ServerboundCustomPayloadPacket::payload);
}
