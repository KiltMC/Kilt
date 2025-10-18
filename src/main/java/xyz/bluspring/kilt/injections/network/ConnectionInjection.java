package xyz.bluspring.kilt.injections.network;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.PacketFlow;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.Consumer;

@FabricInjectedInterface(Connection.class)
public interface ConnectionInjection {
    default Channel channel() {
        throw new RuntimeException("mixin, why didn't you add this");
    }

    default PacketFlow getDirection() {
        throw new RuntimeException("mixin, why didn't you add this");
    }

    default ProtocolInfo<?> getInboundProtocol() {
        throw KiltHelper.createMixinException(ConnectionInjection.class, "getInboundProtocol");
    }
}
