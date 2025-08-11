// TRACKED HASH: 1f34451627a4dce17a1818c0017c9c3364b096ab
package xyz.bluspring.kilt.injects.server.network;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.server.MinecraftServerInjection;
import xyz.bluspring.kilt.injections.server.network.ServerStatusPacketListenerImplInjection;

@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class ServerHandshakePacketListenerImplInject {
    @Shadow @Final private Connection connection;

    @Shadow @Final private MinecraftServer server;

    @Inject(at = @At("HEAD"), method = "handleIntention", cancellable = true)
    public void kilt$handleForgeServerLogin(ClientIntentionPacket clientIntentionPacket, CallbackInfo ci) {
        if (!ServerLifecycleHooks.handleServerLogin(clientIntentionPacket, this.connection))
            ci.cancel();
    }

    @ModifyExpressionValue(method = "handleIntention", at = @At(value = "NEW", target = "(Lnet/minecraft/network/protocol/status/ServerStatus;Lnet/minecraft/network/Connection;)Lnet/minecraft/server/network/ServerStatusPacketListenerImpl;"))
    private ServerStatusPacketListenerImpl kilt$cacheStatusJson(ServerStatusPacketListenerImpl original) {
        ((ServerStatusPacketListenerImplInjection) original).kilt$setStatusCache(((MinecraftServerInjection) this.server).getStatusJson());
        return original;
    }
}