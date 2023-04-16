package xyz.bluspring.kilt.forgeinjects.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.bluspring.kilt.injections.ConnectionInjection;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.function.Consumer;

// A Mixin version of https://github.com/MinecraftForge/MinecraftForge/blob/1.19.x/patches/minecraft/net/minecraft/network/Connection.java.patch
@Mixin(Connection.class)
public class ConnectionInject implements ConnectionInjection {
    @Shadow private Channel channel;
    private Consumer<Connection> activationHandler;

    @Shadow @Final private PacketFlow receiving;

    @NotNull
    @Override
    public Channel channel() {
        return this.channel;
    }

    @NotNull
    @Override
    public PacketFlow getDirection() {
        return this.receiving;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lio/netty/channel/Channel;remoteAddress()Ljava/net/SocketAddress;", shift = At.Shift.AFTER), method = "channelActive")
    public void kilt$acceptActivationHandler(ChannelHandlerContext channelHandlerContext, CallbackInfo ci) {
        if (activationHandler != null)
            activationHandler.accept((Connection) (Object) this);
    }


    @Redirect(at = @At(value = "INVOKE", target = "Lio/netty/channel/ChannelConfig;setAutoRead(Z)Lio/netty/channel/ChannelConfig;"), method = "sendPacket")
    public ChannelConfig kilt$makeEventLoop(ChannelConfig instance, boolean b) {
        this.channel.eventLoop().execute(() -> instance.setAutoRead(false));

        return instance;
    }

    @ModifyVariable(at = @At("STORE"), ordinal = 0, method = "connectToServer")
    private static Connection kilt$registerClientLoginChannel(Connection connection) {
        connection.setActivationHandler(NetworkHooks::registerClientLoginChannel);

        return connection;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/Bootstrap;group(Lio/netty/channel/EventLoopGroup;)Lio/netty/bootstrap/AbstractBootstrap;"), method = "connectToLocalServer", locals = LocalCapture.CAPTURE_FAILHARD)
    private static void kilt$registerClientLoginChannelLocally(SocketAddress address, CallbackInfoReturnable<Connection> cir, Connection connection) {
        connection.setActivationHandler(NetworkHooks::registerClientLoginChannel);
    }

    @Override
    public void setActivationHandler(@NotNull Consumer<Connection> handler) {
        activationHandler = handler;
    }
}
