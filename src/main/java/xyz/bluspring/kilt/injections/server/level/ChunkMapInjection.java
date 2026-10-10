package xyz.bluspring.kilt.injections.server.level;

import java.util.List;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface ChunkMapInjection {
    default List<ServerPlayer> getPlayersWatching(Entity entity) {
        throw KiltHelper.createMixinException(ChunkMapInjection.class, "getPlayersWatching");
    }

    default void scheduleOnMainThreadMailbox(Runnable runnable) {
        throw KiltHelper.createMixinException(ChunkMapInjection.class, "scheduleOnMainThreadMailbox");
    }
}
