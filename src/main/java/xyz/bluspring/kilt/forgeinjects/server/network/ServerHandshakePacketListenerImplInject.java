package xyz.bluspring.kilt.forgeinjects.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakePacketListenerImpl.class)
public class ServerHandshakePacketListenerImplInject {
    @Shadow @Final private Connection connection;

    @Inject(at = @At("HEAD"), method = "handleIntention", cancellable = true)
    public void kilt$handleForgeServerLogin(ClientIntentionPacket clientIntentionPacket, CallbackInfo ci) {
        if (!ServerLifecycleHooks.handleServerLogin(clientIntentionPacket, this.connection))
            ci.cancel();
    }
}
