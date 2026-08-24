package xyz.bluspring.kilt.injections.neoforge.event;

import xyz.bluspring.kilt.util.KiltHelper;

public interface KiltModifiedEventCheckInjection {
    default boolean kilt$wasModified() {
        throw KiltHelper.createMixinException(KiltModifiedEventCheckInjection.class, "kilt$wasModified");
    }
}
