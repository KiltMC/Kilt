package xyz.bluspring.kilt.injections.client.multiplayer;

import net.neoforged.neoforge.client.ExtendedServerListData;

public interface ServerDataInjection {
    default ExtendedServerListData kilt$getNeoForgeData() {
        throw new IllegalStateException();
    }

    default void kilt$setNeoForgeData(ExtendedServerListData data) {
        throw new IllegalStateException();
    }
}
