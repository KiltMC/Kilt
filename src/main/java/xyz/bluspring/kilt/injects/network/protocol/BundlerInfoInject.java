package xyz.bluspring.kilt.injects.network.protocol;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.*;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.network.protocol.BundlerInfoInjection;

import java.util.List;
import java.util.function.Consumer;

@Mixin(BundlerInfo.class)
public interface BundlerInfoInject extends BundlerInfoInjection {
    @Mixin(targets = "net.minecraft.network.protocol.BundlerInfo$0")
    abstract class BundlerInfoAnonymous0Inject<T extends PacketListener, P extends BundlePacket<? super T>> implements BundlerInfoInjection {
        @Shadow @Final PacketType<P> val$bundlePacketType;
        @Shadow @Final BundleDelimiterPacket<T> val$delimiterPacket;

        @Override
        public void unbundlePacket(Packet<?> bundlePacket, Consumer<Packet<?>> packetSender, ChannelHandlerContext context) {
            if (bundlePacket.type() == val$bundlePacketType) {
                P p = (P) bundlePacket;
                List<Packet<?>> packets = NetworkRegistry.filterGameBundlePackets(context, p.subPackets());

                if (packets.isEmpty())
                    return;

                if (packets.size() == 1) {
                    packetSender.accept(packets.get(0));
                    return;
                }

                packetSender.accept(val$delimiterPacket);
                packets.forEach(packetSender);
                packetSender.accept(val$delimiterPacket);
            } else {
                packetSender.accept(bundlePacket);
            }
        }
    }

    @Shadow
    void unbundlePacket(Packet<?> packet, Consumer<Packet<?>> consumer);

    @Override
    default void unbundlePacket(Packet<?> bundlePacket, Consumer<Packet<?>> packetSender, ChannelHandlerContext context) {
        this.unbundlePacket(bundlePacket, packetSender);
    }
}
