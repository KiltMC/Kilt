package xyz.bluspring.kilt.forgeinjects.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplInject {
    @Shadow @Final public Connection connection;

    @Shadow
    ServerLoginPacketListenerImpl.State state;

    @Shadow @Nullable private ServerPlayer delayedAcceptPlayer;

    @Shadow private int tick;

    @Shadow public abstract void disconnect(Component component);

    @Mixin(targets = "net/minecraft/server/network/ServerLoginPacketListenerImpl$1")
    public static class ThreadInject {
        // TODO: there's meant to be a thread group added here, but i don't know how to
        //       redirect super calls, so hope this doesn't bite me in the ass.
    }
}
