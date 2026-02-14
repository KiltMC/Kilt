package xyz.bluspring.kilt.injects.client.multiplayer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.ModMismatchDisconnectedScreen;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.payload.ModdedNetworkPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkQueryPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkSetupFailedPayload;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.network.RegistryFriendlyByteBufInjection;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Mixin(ClientConfigurationPacketListenerImpl.class)
@Implements(value = @Interface(iface = ClientCommonPacketListener.class, prefix = "kilt$i$"))
public abstract class ClientConfigurationPacketListenerImplInject extends ClientCommonPacketListenerImpl implements ClientConfigurationPacketListener {
    private ConnectionType connectionType = ConnectionType.OTHER;
    private boolean initializedConnection = false;
    private Map<ResourceLocation, Component> failureReasons = new HashMap<>();

    protected ClientConfigurationPacketListenerImplInject(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }

    @Inject(method = "handleEnabledFeatures", at = @At("TAIL"))
    private void kilt$handleNeoFallbackDetectionForVanilla(ClientboundUpdateEnabledFeaturesPacket packet, CallbackInfo ci) {
        if (this.connectionType.isOther()) {
            this.initializedConnection = true;
            NetworkRegistry.initializeOtherConnection(this);
        }
    }

    @ModifyExpressionValue(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryFriendlyByteBuf;decorator(Lnet/minecraft/core/RegistryAccess;)Ljava/util/function/Function;"))
    private Function<ByteBuf, RegistryFriendlyByteBuf> kilt$useDecoratorsWithConnectionType(Function<ByteBuf, RegistryFriendlyByteBuf> original) {
        return RegistryFriendlyByteBufInjection.kilt$wrappedDecorator(this.connectionType, original);
    }

    @ModifyExpressionValue(method = "handleConfigurationFinished", at = @At(value = "NEW", target = "(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/telemetry/WorldSessionTelemetryManager;Lnet/minecraft/core/RegistryAccess$Frozen;Lnet/minecraft/world/flag/FeatureFlagSet;Ljava/lang/String;Lnet/minecraft/client/multiplayer/ServerData;Lnet/minecraft/client/gui/screens/Screen;Ljava/util/Map;Lnet/minecraft/client/gui/components/ChatComponent$State;ZLjava/util/Map;Lnet/minecraft/server/ServerLinks;)Lnet/minecraft/client/multiplayer/CommonListenerCookie;"))
    private CommonListenerCookie kilt$addConnectionTypeToCookie(CommonListenerCookie original) {
        original.kilt$setConnectionType(this.connectionType);
        return original;
    }

    @Inject(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void kilt$handleConfigurationConnection(ClientboundFinishConfigurationPacket packet, CallbackInfo ci) {
        if (!this.initializedConnection && this.connectionType.isOther()) {
            NetworkRegistry.initializeOtherConnection(this);
        }

        NetworkRegistry.onConfigurationFinished(this);
    }

    @Intrinsic(displace = true)
    public void kilt$i$handleCustomPayload(ClientboundCustomPayloadPacket packet) {
        // Handle the query payload by responding with the client's network channels. Update the connection type accordingly.
        if (packet.payload() instanceof ModdedNetworkQueryPayload) {
            this.connectionType = ConnectionType.NEOFORGE;
            NetworkRegistry.onNetworkQuery(this);
            return;
        }

        // Receiving a modded network payload implies a successful negotiation by the server.
        if (packet.payload() instanceof ModdedNetworkPayload payload) {
            this.initializedConnection = true;
            NetworkRegistry.initializeNeoForgeConnection(this, payload.setup());
            return;
        }

        // Receiving a setup failed payload will be followed by a disconnect from the server, so we don't need to disconnect manually here.
        if (packet.payload() instanceof ModdedNetworkSetupFailedPayload payload) {
            this.failureReasons = payload.failureReasons();
            return;
        }

        // Receiving a brand payload without having transitioned to a Neo connection implies a non-modded connection has begun.
        if (this.connectionType.isOther() && packet.payload() instanceof BrandPayload) {
            this.initializedConnection = true;
            NetworkRegistry.initializeOtherConnection(this);
        }

        // Fallback to super for un/register, modded, and vanilla payloads.
        super.handleCustomPayload(packet);
    }

    // Kilt: fine i'll do it
    @Override
    protected Screen createDisconnectScreen(DisconnectionDetails details) {
        var screen = super.createDisconnectScreen(details);
        if (this.failureReasons.isEmpty())
            return screen;

        return new ModMismatchDisconnectedScreen(screen, Component.translatable("disconnect.lost"), this.failureReasons);
    }

    @Override
    public ConnectionType getConnectionType() {
        return this.connectionType;
    }
}
