package xyz.bluspring.kilt.injections.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import net.neoforged.neoforge.client.model.data.ModelData;
import xyz.bluspring.kilt.util.KiltHelper;

public interface RenderChunkRegionInjection {
    default void kilt$setModelDataSnapshot(Long2ObjectFunction<ModelData> modelDataSnapshot) {
        throw KiltHelper.createMixinException(RenderChunkRegionInjection.class, "kilt$setModelDataSnapshot");
    }
}
