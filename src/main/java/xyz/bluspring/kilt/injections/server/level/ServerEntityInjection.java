package xyz.bluspring.kilt.injections.server.level;

import net.neoforged.neoforge.network.bundle.PacketAndPayloadAcceptor;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;

public interface ServerEntityInjection {
    default void sendPairingData(ServerPlayer player, PacketAndPayloadAcceptor<ClientGamePacketListener> acceptor) {
        throw KiltHelper.createMixinException(ServerEntityInjection.class, "sendPairingData");
    }
}
