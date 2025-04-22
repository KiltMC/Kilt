package xyz.bluspring.kilt.injections.server.level;

import io.github.fabricators_of_create.porting_lib.chunk.loading.extensions.ServerChunkCacheExtension;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

public interface ServerChunkCacheInjection extends ServerChunkCacheExtension {
    default <T> void addRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object, boolean forceTicks) {
        throw new IllegalStateException();
    }

    default <T> void removeRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object, boolean forceTicks) {
        throw new IllegalStateException();
    }
}
