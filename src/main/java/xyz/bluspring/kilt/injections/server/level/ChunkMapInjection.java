package xyz.bluspring.kilt.injections.server.level;

import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import xyz.bluspring.kilt.util.KiltHelper;

public interface ChunkMapInjection {
    default void scheduleOnMainThreadMailbox(ChunkTaskPriorityQueueSorter.Message<Runnable> msg) {
        throw KiltHelper.createMixinException(ChunkMapInjection.class, "scheduleOnMainThreadMailbox");
    }
}
