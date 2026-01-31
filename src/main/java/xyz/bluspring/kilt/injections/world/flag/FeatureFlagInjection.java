package xyz.bluspring.kilt.injections.world.flag;

import xyz.bluspring.kilt.util.KiltHelper;

public interface FeatureFlagInjection {
    default int kilt$extMaskIndex() {
        throw KiltHelper.createMixinException(FeatureFlagInjection.class, "kilt$extMaskIndex");
    }

    default void kilt$setExtMaskIndex(int index) {
        throw KiltHelper.createMixinException(FeatureFlagInjection.class, "kilt$setExtMaskIndex");
    }

    default boolean isModded() {
        throw KiltHelper.createMixinException(FeatureFlagInjection.class, "isModded");
    }

    default void kilt$setModded(boolean modded) {
        throw KiltHelper.createMixinException(FeatureFlagInjection.class, "kilt$setModded");
    }
}
