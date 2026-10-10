package xyz.bluspring.kilt.injections.data.tags;

import xyz.bluspring.kilt.util.KiltHelper;

public interface TagsProviderInjection {
    default void kilt$setModId(String modId) {
        throw KiltHelper.createMixinException(TagsProviderInjection.class, "kilt$setModId");
    }
}
