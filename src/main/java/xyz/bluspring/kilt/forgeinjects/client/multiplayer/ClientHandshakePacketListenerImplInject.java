// TRACKED HASH: 2343eb933a57f5c34b6329cd7793fe4364810631
package xyz.bluspring.kilt.forgeinjects.client.multiplayer;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraftforge.network.NetworkHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientHandshakePacketListenerImpl.class)
public class ClientHandshakePacketListenerImplInject {
    @Shadow @Final private Connection connection;

    @Inject(method = "handleCustomQuery", at = @At("HEAD"), cancellable = true)
    public void kilt$checkCustomPayload(ClientboundCustomQueryPacket packet, CallbackInfo ci) {
        if (NetworkHooks.onCustomPayload(packet, this.connection))
            ci.cancel();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setProtocol(Lnet/minecraft/network/ConnectionProtocol;)V", shift = At.Shift.AFTER), method = "handleGameProfile")
    public void kilt$handleForgeClientLoginSuccess(ClientboundGameProfilePacket clientboundGameProfilePacket, CallbackInfo ci) {
        NetworkHooks.handleClientLoginSuccess(this.connection);
    }
}