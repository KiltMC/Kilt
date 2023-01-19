package xyz.bluspring.kilt.injections.network;

import net.minecraftforge.network.ServerStatusPing;

public interface ServerStatusInjection {
    default ServerStatusPing getForgeData() {
        throw new IllegalStateException();
    }

    default void setForgeData(ServerStatusPing data) {
        throw new IllegalStateException();
    }

    default String getJson() {
        throw new IllegalStateException();
    }

    default void invalidateJson() {
        throw new IllegalStateException();
    }
}
