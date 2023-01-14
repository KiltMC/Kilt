package xyz.bluspring.kilt.injections;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;

import java.util.function.Consumer;

public interface ConnectionInjection {
    default Channel channel() {
        throw new RuntimeException("mixin, why didn't you add this");
    }

    default PacketFlow getDirection() {
        throw new RuntimeException("mixin, why didn't you add this");
    }

    default void setActivationHandler(Consumer<Connection> handler) {
        throw new RuntimeException("mixin, why didn't you add this");
    }
}
