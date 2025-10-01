package xyz.bluspring.kilt.injections.client.player;

import net.minecraft.client.player.LocalPlayer;
import xyz.bluspring.kilt.util.KiltHelper;

public interface LocalPlayerInjection {
    default void updateSyncFields(LocalPlayer old) {
        throw KiltHelper.createMixinException(LocalPlayerInjection.class, "updateSyncFields");
    }
}
