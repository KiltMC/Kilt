package xyz.bluspring.kilt.injections.client.data.models;

import xyz.bluspring.kilt.util.KiltHelper;

public interface ModelProviderInjection {
    default String kilt$getModId() {
        throw KiltHelper.createMixinException(ModelProviderInjection.class, "kilt$getModId");
    }

    default void kilt$setModId(String modId) {
        throw KiltHelper.createMixinException(ModelProviderInjection.class, "kilt$setModId");
    }
}
