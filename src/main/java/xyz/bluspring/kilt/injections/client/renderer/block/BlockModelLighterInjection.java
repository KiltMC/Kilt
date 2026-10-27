package xyz.bluspring.kilt.injections.client.renderer.block;

import xyz.bluspring.kilt.util.KiltHelper;

public interface BlockModelLighterInjection {
    default void reset() {
        throw KiltHelper.createMixinException(BlockModelLighterInjection.class, "reset");
    }
}
