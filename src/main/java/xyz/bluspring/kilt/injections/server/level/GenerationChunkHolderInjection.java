package xyz.bluspring.kilt.injections.server.level;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.level.chunk.LevelChunk;

public interface GenerationChunkHolderInjection {
    default LevelChunk kilt$getCurrentlyLoading() {
        throw KiltHelper.createMixinException(GenerationChunkHolderInjection.class, "kilt$getCurrentlyLoading");
    }

    default void kilt$setCurrentlyLoading(LevelChunk chunk) {
        throw KiltHelper.createMixinException(GenerationChunkHolderInjection.class, "kilt$setCurrentlyLoading");
    }
}
