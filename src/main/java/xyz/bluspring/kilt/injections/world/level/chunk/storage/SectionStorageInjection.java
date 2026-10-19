package xyz.bluspring.kilt.injections.world.level.chunk.storage;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.level.ChunkPos;

public interface SectionStorageInjection {
    default void remove(ChunkPos chunkPos) {
        throw KiltHelper.createMixinException(SectionStorageInjection.class, "remove");
    }
}
