package xyz.bluspring.kilt.injections;

import net.minecraft.core.BlockPos;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.UUID;

public interface ForcedChunksSavedDataInjection {
    default ForgeChunkManager.TicketTracker<BlockPos> getBlockForcedChunks() {
        throw new IllegalStateException();
    }

    default ForgeChunkManager.TicketTracker<UUID> getEntityForcedChunks() {
        throw new IllegalStateException();
    }
}
