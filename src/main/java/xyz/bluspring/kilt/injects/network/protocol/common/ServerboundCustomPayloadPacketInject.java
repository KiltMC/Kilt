package xyz.bluspring.kilt.injects.network.protocol.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.network.protocol.common.ServerboundCustomPayloadPacketInjection;
import xyz.bluspring.kilt.injections.network.protocol.common.custom.CustomPacketPayloadInjection;

import java.util.List;

@Mixin(ServerboundCustomPayloadPacket.class)
public abstract class ServerboundCustomPayloadPacketInject implements ServerboundCustomPayloadPacketInjection {
    @CreateStatic
    private static final StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> CONFIG_STREAM_CODEC = ServerboundCustomPayloadPacketInjection.CONFIG_STREAM_CODEC;

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;)Lnet/minecraft/network/codec/StreamCodec;", ordinal = 0))
    private static <B extends FriendlyByteBuf> StreamCodec<B, CustomPacketPayload> kilt$addProtocolsToCodec(CustomPacketPayload.FallbackProvider<B> fallbackProvider, List<CustomPacketPayload.TypeAndCodec<? super B, ?>> list, Operation<StreamCodec<B, CustomPacketPayload>> original) {
        try {
            CustomPacketPayloadInjection.kilt$protocol.set(ConnectionProtocol.PLAY);
            CustomPacketPayloadInjection.kilt$packetFlow.set(PacketFlow.SERVERBOUND);
            return original.call(fallbackProvider, list);
        } finally {
            CustomPacketPayloadInjection.kilt$protocol.remove();
            CustomPacketPayloadInjection.kilt$packetFlow.remove();
        }
    }
}
