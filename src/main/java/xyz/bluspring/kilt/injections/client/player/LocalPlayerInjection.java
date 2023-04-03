package xyz.bluspring.kilt.injections.client.player;

import net.minecraft.client.player.LocalPlayer;

public interface LocalPlayerInjection {
    default void updateSyncFields(LocalPlayer old) {
        throw new IllegalStateException();
    }
}
