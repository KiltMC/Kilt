package xyz.bluspring.kilt.injections.world.flag;

import xyz.bluspring.kilt.util.KiltHelper;

public interface FeatureFlagSetInjection {
    default long[] kilt$extendedMask() {
        throw KiltHelper.createMixinException(FeatureFlagSetInjection.class, "kilt$extendedMask");
    }

    default void kilt$setExtendedMask(long[] extendedMask) {
        throw KiltHelper.createMixinException(FeatureFlagSetInjection.class, "kilt$setExtendedMask");
    }
}
