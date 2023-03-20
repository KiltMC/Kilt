package xyz.bluspring.kilt.forgeinjects.server.level;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.server.level.ServerChunkCacheInjection;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheInject implements ServerChunkCacheInjection {
    @Shadow public abstract <T> void addRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object);

    @Shadow public abstract <T> void removeRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object);

    @Override
    public <T> void addRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object, boolean forceTicks) {
        // TODO: actually do something with forceTicks
        this.addRegionTicket(ticketType, chunkPos, i, object);
    }

    @Override
    public <T> void removeRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object, boolean forceTicks) {
        this.removeRegionTicket(ticketType, chunkPos, i, object);
    }
}
