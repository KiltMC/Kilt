package xyz.bluspring.kilt.injections.server.level;

import net.minecraft.world.level.chunk.LevelChunk;

public interface ChunkHolderInjection {
    LevelChunk kilt$getCurrentlyLoading();
    void kilt$setCurrentlyLoading(LevelChunk chunk);
}
