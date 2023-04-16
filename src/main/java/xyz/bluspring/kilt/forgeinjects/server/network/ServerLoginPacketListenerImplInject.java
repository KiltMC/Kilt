package xyz.bluspring.kilt.forgeinjects.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplInject {
    @Shadow @Final public Connection connection;

    @Shadow
    ServerLoginPacketListenerImpl.State state;

    @Shadow @Nullable private ServerPlayer delayedAcceptPlayer;

    @Shadow private int tick;

    @Shadow public abstract void disconnect(Component component);

    @Inject(method = "handleCustomQueryPacket", at = @At("HEAD"), cancellable = true)
    public void kilt$handleCustomQuery(ServerboundCustomQueryPacket packet, CallbackInfo ci) {
        if (NetworkHooks.onCustomPayload(packet, this.connection)) {
            ci.cancel();
        }
    }

    @Mixin(targets = "net/minecraft/server/network/ServerLoginPacketListenerImpl$1")
    public static class ThreadInject {
        // TODO: there's meant to be a thread group added here, but i don't know how to
        //       redirect super calls, so hope this doesn't bite me in the ass.
    }
}
