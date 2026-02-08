package xyz.bluspring.kilt.injects.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.neoforged.neoforge.common.extensions.IClientCommonPacketListenerExtension;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.payload.CommonRegisterPayload;
import net.neoforged.neoforge.network.payload.CommonVersionPayload;
import net.neoforged.neoforge.network.payload.MinecraftRegisterPayload;
import net.neoforged.neoforge.network.payload.MinecraftUnregisterPayload;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplInject implements ClientCommonPacketListener, IClientCommonPacketListenerExtension {
    @Shadow
    @Final
    protected Connection connection;
    protected ConnectionType connectionType = ConnectionType.OTHER;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$setConnectionTypeBasedOnCookie(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        this.connectionType = commonListenerCookie.connectionType();
    }

    @Inject(method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V", at = @At("HEAD"), cancellable = true)
    private void kilt$handleNeoPayloads(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (packet.payload() instanceof MinecraftRegisterPayload payload) {
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
        }

        if (NetworkRegistry.isModdedPayload(packet.payload())) {
            NetworkRegistry.handleModdedPayload(this, packet);
            ci.cancel();
        }
    }

    /*@Inject(method = "send", at = @At("HEAD")) // Kilt: no.
    private void kilt$validateNetworkPacket(Packet<?> packet, CallbackInfo ci) {
        NetworkRegistry.checkPacket(packet, this);
    }*/

    @Override
    public Connection getConnection() {
        return this.connection;
    }
}
