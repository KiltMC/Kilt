package xyz.bluspring.kilt.injections.server.level;

import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

public interface DistanceManagerInjection {
    <T> void addRegionTicket(TicketType type, ChunkPos pos, int distance, T value, boolean forceTicks);
    <T> void removeRegionTicket(TicketType type, ChunkPos pos, int distance, T value, boolean forceTicks);
}
