package xyz.bluspring.kilt.injections.world.level;

import java.util.UUID;

import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public interface TicketStorageInjection {
    default ForcedChunkManager.TicketTracker<BlockPos> getBlockForcedChunks() {
        throw KiltHelper.createMixinException(TicketStorageInjection.class, "getBlockForcedChunks");
    }

    default ForcedChunkManager.TicketTracker<UUID> getEntityForcedChunks() {
        throw KiltHelper.createMixinException(TicketStorageInjection.class, "getEntityForcedChunks");
    }

    default boolean shouldForceNaturalSpawning(ChunkPos pos) {
        throw KiltHelper.createMixinException(TicketStorageInjection.class, "shouldForceNaturalSpawning");
    }
}
