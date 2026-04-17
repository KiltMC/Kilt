package xyz.bluspring.kilt.injections.client.particle;

import xyz.bluspring.kilt.util.KiltHelper;

public interface ParticleRenderTypeInjection {
    default boolean isTranslucent() {
        throw KiltHelper.createMixinException(ParticleRenderTypeInjection.class, "isTranslucent");
    }
}
