package xyz.bluspring.kilt.injections.network;

import net.minecraftforge.network.ServerStatusPing;

import java.util.Optional;

public interface ServerStatusInjection {
    default Optional<ServerStatusPing> forgeData() {
        throw new IllegalStateException();
    }

    default void setForgeData(Optional<ServerStatusPing> data) {
        throw new IllegalStateException();
    }
}
