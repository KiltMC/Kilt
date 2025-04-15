package xyz.bluspring.kilt.injections.network.protocol.status;

import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import org.jetbrains.annotations.Nullable;

public interface ClientboundStatusResponsePacketInjection {
    static ClientboundStatusResponsePacket create(ServerStatus status, @Nullable String cachedStatus) {
        var packet = new ClientboundStatusResponsePacket(status);
        ((ClientboundStatusResponsePacketInjection) (Object) packet).kilt$setCachedStatus(cachedStatus);

        return packet;
    }

    void kilt$setCachedStatus(String data);
    @Nullable String cachedStatus();
}
