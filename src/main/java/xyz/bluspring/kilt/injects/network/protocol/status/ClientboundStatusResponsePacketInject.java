package xyz.bluspring.kilt.injects.network.protocol.status;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.network.protocol.status.ClientboundStatusResponsePacketInjection;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;

@Mixin(ClientboundStatusResponsePacket.class)
public abstract class ClientboundStatusResponsePacketInject implements ClientboundStatusResponsePacketInjection {
    @Shadow @Final @Mutable public static StreamCodec<ByteBuf, ClientboundStatusResponsePacket> STREAM_CODEC;
    @CreateStatic private static StreamCodec<ByteBuf, ClientboundStatusResponsePacket> VANILLA_STREAM_CODEC;

    @Nullable @Unique
    private String cachedStatus;

    @Unique
    private boolean kilt$shouldWriteStatus = false;

    public ClientboundStatusResponsePacketInject(ServerStatus status) {}

    @CreateInitializer
    public ClientboundStatusResponsePacketInject(ServerStatus status, @Nullable String cachedStatus) {
        this(status);
        this.kilt$setCachedStatus(cachedStatus);
        this.kilt$shouldWriteStatus = true;
    }

    @Override
    public @Nullable String cachedStatus() {
        return cachedStatus;
    }

    @Override
    public void kilt$setCachedStatus(String data) {
        this.cachedStatus = data;
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$wrapWithStatusCache(CallbackInfo ci) {
        VANILLA_STREAM_CODEC = STREAM_CODEC;

        STREAM_CODEC = StreamCodec.of((buf, packet) -> {
            if (packet.cachedStatus() != null)
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.cachedStatus());
            else
                VANILLA_STREAM_CODEC.encode(buf, packet);
        }, VANILLA_STREAM_CODEC);
    }
}
