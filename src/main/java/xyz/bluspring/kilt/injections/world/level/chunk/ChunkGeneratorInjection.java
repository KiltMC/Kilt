package xyz.bluspring.kilt.injections.world.level.chunk;

import xyz.bluspring.kilt.util.KiltHelper;

public interface ChunkGeneratorInjection {
    default void refreshFeaturesPerStep() {
        throw KiltHelper.createMixinException(ChunkGeneratorInjection.class, "refreshFeaturesPerStep");
    }
}
