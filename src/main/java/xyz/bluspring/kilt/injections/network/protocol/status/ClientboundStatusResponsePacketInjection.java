package xyz.bluspring.kilt.injections.network.protocol.status;

import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;

@FabricInjectedInterface(ClientboundStatusResponsePacket.class)
public interface ClientboundStatusResponsePacketInjection {
    static ClientboundStatusResponsePacket create(ServerStatus status, @Nullable String cachedStatus) {
        var packet = new ClientboundStatusResponsePacket(status);
        packet.kilt$setCachedStatus(cachedStatus);

        return packet;
    }

    default void kilt$setCachedStatus(String data) {
        throw KiltHelper.createMixinException(ClientboundStatusResponsePacketInjection.class, "kilt$setCachedStatus");
    }

    default @Nullable String cachedStatus() {
        throw KiltHelper.createMixinException(ClientboundStatusResponsePacketInjection.class, "cachedStatus");
    }
}
