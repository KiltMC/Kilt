package xyz.bluspring.kilt.injections.world.level.chunk.status;

import java.util.EnumSet;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.level.levelgen.Heightmap;

public interface ChunkStatusInjection {
    default EnumSet<Heightmap.Types> getChunkSaveHeightmaps() {
        throw KiltHelper.createMixinException(ChunkStatusInjection.class, "getChunkSaveHeightmaps");
    }
}
