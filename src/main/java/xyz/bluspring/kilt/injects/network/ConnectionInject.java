// TRACKED HASH: b6113e1d44b934ff02eb6b5b554fa8ccbd085e79
package xyz.bluspring.kilt.injects.network;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.neoforged.neoforge.network.connection.ConnectionUtils;
import net.neoforged.neoforge.network.filters.NetworkFilters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.network.ConnectionInjection;

import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketBundleUnpacker;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.PacketFlow;

@Mixin(Connection.class)
public abstract class ConnectionInject implements ConnectionInjection {
    @Shadow private Channel channel;
    @Shadow @Final private PacketFlow receiving;
    @Shadow @Final private static Logger LOGGER;

    @Unique @Nullable private ProtocolInfo<?> inboundProtocol;

    @Inject(method = "channelActive", at = @At("TAIL"))
    private void kilt$setNeoConnection(ChannelHandlerContext ctx, CallbackInfo ci) {
        ConnectionUtils.setConnection(ctx, (Connection) (Object) this);
    }

    @Inject(method = "channelInactive", at = @At("TAIL"))
    private void kilt$removeNeoConnection(ChannelHandlerContext ctx, CallbackInfo ci) {
        ConnectionUtils.removeConnection(ctx);
    }

    @Inject(method = "exceptionCaught", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketListener;createDisconnectionInfo(Lnet/minecraft/network/chat/Component;Ljava/lang/Throwable;)Lnet/minecraft/network/DisconnectionDetails;"))
    private void kilt$printCriticalNetworkExceptions(ChannelHandlerContext ctx, Throwable cause, CallbackInfo ci, @Local(name = "listener") PacketListener packetListener) {
        var protocol = packetListener.protocol();

        if (protocol == ConnectionProtocol.CONFIGURATION || protocol == ConnectionProtocol.PLAY) {
            LOGGER.error("Exception caught in connection", cause);
        }
    }

    @Definition(id = "packetListener", field = "Lnet/minecraft/network/Connection;packetListener:Lnet/minecraft/network/PacketListener;")
    @Definition(id = "packetInfo", local = @Local(type = PacketListener.class, argsOnly = true))
    @Expression("this.packetListener = packetInfo")
    @Inject(method = "setupInboundProtocol", at = @At("MIXINEXTRAS:EXPRESSION"))
    private <T extends PacketListener> void kilt$storeCurrentInboundProtocol(ProtocolInfo<T> protocol, T packetListener, CallbackInfo ci) {
        this.inboundProtocol = protocol;
    }

    @Inject(method = "lambda$setupOutboundProtocol$0", at = @At("TAIL"))
    private static void kilt$injectNetworkFiltersIfNecessary(PacketBundleUnpacker newUnbundler, ChannelHandlerContext ctx, CallbackInfo ci) {
        NetworkFilters.injectIfNecessary(ConnectionUtils.getConnection(ctx));
    }

    // Kilt: still not handling dual-stack

    @NotNull
    @Override
    @Accessor("channel")
    public abstract Channel channel();

    @NotNull
    @Override
    @Accessor("receiving")
    public abstract PacketFlow getDirection();

    @Override
    public @Nullable ProtocolInfo<?> getInboundProtocol() {
        return this.inboundProtocol;
    }
}
