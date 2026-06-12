package xyz.bluspring.kilt.mixin.workarounds.limit_packet_splitting;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import net.neoforged.neoforge.network.filters.GenericPacketSplitter;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Mixin(GenericPacketSplitter.class)
public abstract class GenericPacketSplitterMixin {
    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Ljava/util/List;)V", at = @At("HEAD"), cancellable = true)
    private void kilt$guardToNeoPacketsOnly(ChannelHandlerContext ctx, Packet<?> packet, List<Object> out, CallbackInfo ci) {
        CustomPacketPayload.Type<?> type = null;

        if (packet instanceof ServerboundCustomPayloadPacket(CustomPacketPayload payload)) {
            type = payload.type();
        } else if (packet instanceof ClientboundCustomPayloadPacket(CustomPacketPayload payload)) {
            type = payload.type();
        }

        if (type != null) {
            // Block all non-Neo packets, otherwise mods like Supplementaries/Moonlight will completely fail due to how they have their packets set up.
            if (!NetworkRegistry.kilt$payloadExists(ConnectionProtocol.PLAY, type.id()) && !NetworkRegistry.kilt$payloadExists(ConnectionProtocol.CONFIGURATION, type.id())) {
                out.add(packet);
                ci.cancel();
            }
        }
    }
}
