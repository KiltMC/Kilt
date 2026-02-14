package xyz.bluspring.kilt.injects.server.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.payload.CommonRegisterPayload;
import net.neoforged.neoforge.network.payload.CommonVersionPayload;
import net.neoforged.neoforge.network.payload.MinecraftRegisterPayload;
import net.neoforged.neoforge.network.payload.MinecraftUnregisterPayload;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.server.network.CommonListenerCookieInjection;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplInject implements ServerCommonPacketListener {
    @Shadow @Final protected Connection connection;
    @Shadow protected abstract GameProfile playerProfile();
    @Shadow private int latency;
    @Shadow @Final private boolean transferred;
    @Shadow @Final protected MinecraftServer server;

    @Shadow public void handleCustomPayload(ServerboundCustomPayloadPacket serverboundCustomPayloadPacket) {
        throw new IllegalStateException();
    }

    @Shadow public void handlePong(ServerboundPongPacket serverboundPongPacket) {
        throw new IllegalStateException();
    }

    @Unique protected ConnectionType connectionType = ConnectionType.OTHER;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$setConnectionTypeFromCookie(MinecraftServer server, Connection connection, CommonListenerCookie cookie, CallbackInfo ci) {
        this.connectionType = cookie.connectionType();
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void kilt$handleNeoPayloads(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        // Kilt: Fabric handles this
        /*if (packet.payload() instanceof MinecraftRegisterPayload payload) {
            NetworkRegistry.onMinecraftRegister(this.getConnection(), payload.newChannels());
            ci.cancel();
            return;
        }

        if (packet.payload() instanceof MinecraftUnregisterPayload payload) {
            NetworkRegistry.onMinecraftUnregister(this.getConnection(), payload.forgottenChannels());
            ci.cancel();
            return;
        }

        if (packet.payload() instanceof CommonVersionPayload payload) {
            NetworkRegistry.checkCommonVersion(this, payload);
            ci.cancel();
            return;
        }

        if (packet.payload() instanceof CommonRegisterPayload payload) {
            NetworkRegistry.onCommonRegister(this, payload);
            ci.cancel();
            return;
        }*/

        // Kilt: We handle this via Fabric API
        /*if (NetworkRegistry.isModdedPayload(packet.payload())) {
            NetworkRegistry.handleModdedPayload(this, packet);

            if (NetworkRegistry.kilt$wasHandled.get()) {
                ci.cancel();
                NetworkRegistry.kilt$wasHandled.remove();
            }
        }*/
    }

    /*@Inject(method = "send", at = @At("HEAD")) // Kilt: no.
    private void kilt$validateNetworkPacket(Packet<?> packet, CallbackInfo ci) {
        NetworkRegistry.checkPacket(packet, this);
    }*/

    protected CommonListenerCookie createCookie(ClientInformation info, ConnectionType type) {
        var cookie = new CommonListenerCookie(this.playerProfile(), this.latency, info, this.transferred);
        cookie.kilt$setConnectionType(type);
        return cookie;
    }


    @Override
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public ReentrantBlockableEventLoop<?> getMainThreadEventLoop() {
        return this.server;
    }

    @Override
    public ConnectionType getConnectionType() {
        return this.connectionType;
    }
}
