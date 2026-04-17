package xyz.bluspring.kilt.injections.client.renderer.chunk;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

public interface RenderRegionCacheInjection {
    default void kilt$setNullForEmpty(boolean value) {
        throw KiltHelper.createMixinException(RenderRegionCacheInjection.class, "kilt$setNullForEmpty");
    }

    default RenderChunkRegion createRegion(Level level, SectionPos pos, boolean nullForEmpty) {
        throw KiltHelper.createMixinException(RenderRegionCacheInjection.class, "createRegion");
    }
}
