package xyz.bluspring.kilt.injections.client.multiplayer;

import net.minecraftforge.client.ExtendedServerListData;

public interface ServerDataInjection {
    default ExtendedServerListData getForgeData() {
        throw new IllegalStateException();
    }

    default void setForgeData(ExtendedServerListData data) {
        throw new IllegalStateException();
    }
}
