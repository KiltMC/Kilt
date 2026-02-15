package xyz.bluspring.kilt.injections.server.level;

import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;

public interface ChunkMapInjection {
    default void scheduleOnMainThreadMailbox(ChunkTaskPriorityQueueSorter.Message<Runnable> msg) {
        throw KiltHelper.createMixinException(ChunkMapInjection.class, "scheduleOnMainThreadMailbox");
    }

    default List<ServerPlayer> getPlayersWatching(Entity entity) {
        throw KiltHelper.createMixinException(ChunkMapInjection.class, "getPlayersWatching");
    }
}
