package xyz.bluspring.kilt.forgeinjects.server.network;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.network.protocol.status.ClientboundStatusResponsePacketInjection;
import xyz.bluspring.kilt.injections.server.network.ServerStatusPacketListenerImplInjection;

@Mixin(ServerStatusPacketListenerImpl.class)
public abstract class ServerStatusPacketListenerImplInject implements ServerStatusPacketListenerImplInjection {
    @Unique @Nullable
    private String statusCache;

    public ServerStatusPacketListenerImplInject(ServerStatus status, Connection connection) {}

    @CreateInitializer
    public ServerStatusPacketListenerImplInject(ServerStatus status, Connection connection, @Nullable String statusCache) {
        this(status, connection);
        this.statusCache = statusCache;
    }

    @Override
    public void kilt$setStatusCache(String statusCache) {
        this.statusCache = statusCache;
    }

    @ModifyExpressionValue(method = "handleStatusRequest", at = @At(value = "NEW", target = "(Lnet/minecraft/network/protocol/status/ServerStatus;)Lnet/minecraft/network/protocol/status/ClientboundStatusResponsePacket;"))
    private ClientboundStatusResponsePacket kilt$addStatusCacheToRequest(ClientboundStatusResponsePacket original) {
        ((ClientboundStatusResponsePacketInjection) (Object) original).kilt$setCachedStatus(this.statusCache);

        return original;
    }
}
