package xyz.bluspring.kilt.injections.network.protocol;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.protocol.Packet;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.Consumer;

public interface BundlerInfoInjection {
    default void unbundlePacket(Packet<?> bundlePacket, Consumer<Packet<?>> packetSender, ChannelHandlerContext context) {
        throw KiltHelper.createMixinException(BundlerInfoInjection.class, "unbundlePacket");
    }
}
