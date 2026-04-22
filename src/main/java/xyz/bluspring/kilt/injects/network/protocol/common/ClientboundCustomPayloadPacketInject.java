package xyz.bluspring.kilt.injects.network.protocol.common;

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.StupidWorkarounds;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Mixin(ClientboundCustomPayloadPacket.class)
public abstract class ClientboundCustomPayloadPacketInject {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;)Lnet/minecraft/network/codec/StreamCodec;", ordinal = 0))
    private static <B extends FriendlyByteBuf> StreamCodec<B, CustomPacketPayload> kilt$addProtocolsToCodec(CustomPacketPayload.FallbackProvider<B> fallbackProvider, List<CustomPacketPayload.TypeAndCodec<? super B, ?>> list, Operation<StreamCodec<B, CustomPacketPayload>> original) {
        try {
            StupidWorkarounds.kilt$protocol.set(ConnectionProtocol.PLAY);
            StupidWorkarounds.kilt$packetFlow.set(PacketFlow.CLIENTBOUND);
            return original.call(fallbackProvider, list);
        } finally {
            StupidWorkarounds.kilt$protocol.remove();
            StupidWorkarounds.kilt$packetFlow.remove();
        }
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;)Lnet/minecraft/network/codec/StreamCodec;", ordinal = 1))
    private static <B extends FriendlyByteBuf> StreamCodec<B, CustomPacketPayload> kilt$addConfigProtocolsToCodec(CustomPacketPayload.FallbackProvider<B> fallbackProvider, List<CustomPacketPayload.TypeAndCodec<? super B, ?>> list, Operation<StreamCodec<B, CustomPacketPayload>> original) {
        try {
            StupidWorkarounds.kilt$protocol.set(ConnectionProtocol.CONFIGURATION);
            StupidWorkarounds.kilt$packetFlow.set(PacketFlow.CLIENTBOUND);
            return original.call(fallbackProvider, list);
        } finally {
            StupidWorkarounds.kilt$protocol.remove();
            StupidWorkarounds.kilt$packetFlow.remove();
        }
    }
}
