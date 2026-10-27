package xyz.bluspring.kilt.injections.server.players;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.level.storage.PlayerDataStorage;

public interface PlayerListInjection {
    default PlayerDataStorage getPlayerIo() {
        throw KiltHelper.createMixinException(PlayerListInjection.class, "getPlayerIo");
    }
}
