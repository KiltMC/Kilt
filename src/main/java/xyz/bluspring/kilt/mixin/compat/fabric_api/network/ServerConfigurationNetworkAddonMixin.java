package xyz.bluspring.kilt.mixin.compat.fabric_api.network;

import java.util.Map;

import net.neoforged.neoforge.network.payload.ModdedNetworkQueryPayload;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;

@Mixin(ServerConfigurationNetworkAddon.class)
public abstract class ServerConfigurationNetworkAddonMixin extends AbstractChanneledNetworkAddon<ServerConfigurationNetworking.ConfigurationPacketHandler<?>> {
    @Shadow
    @Final
    private ServerConfigurationPacketListenerImpl listener;

    protected ServerConfigurationNetworkAddonMixin(GlobalReceiverRegistry<ServerConfigurationNetworking.ConfigurationPacketHandler<?>> receiver, Connection connection, String description) {
        super(receiver, connection, description);
    }

    @Inject(method = "startConfiguration", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/networking/server/ServerConfigurationNetworkAddon;sendInitialChannelRegistrationPacket()V", shift = At.Shift.AFTER))
    private void kilt$setupNeoConnection(CallbackInfoReturnable<Boolean> cir) {
        this.sendPacket(this.createRegistrationPayload(RegistrationPayload.UNREGISTER, NetworkRegistry.getInitialServerUnregisterChannels()));
        this.sendPacket(this.createRegistrationPayload(RegistrationPayload.REGISTER, NetworkRegistry.getInitialListeningChannels(this.listener.flow())));
        this.sendPacket(new ModdedNetworkQueryPayload(Map.of()));
    }
}
