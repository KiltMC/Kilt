package xyz.bluspring.kilt.injections.util;

import xyz.bluspring.kilt.util.KiltHelper;

public interface TriStateInjection {
    default boolean isTrue() {
        throw KiltHelper.createMixinException(TriStateInjection.class, "isTrue");
    }

    default boolean isDefault() {
        throw KiltHelper.createMixinException(TriStateInjection.class, "isDefault");
    }

    default boolean isFalse() {
        throw KiltHelper.createMixinException(TriStateInjection.class, "isFalse");
    }
}
