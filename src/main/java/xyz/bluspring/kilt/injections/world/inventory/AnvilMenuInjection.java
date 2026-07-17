package xyz.bluspring.kilt.injections.world.inventory;

import xyz.bluspring.kilt.util.KiltHelper;

public interface AnvilMenuInjection {
    default void setMaximumCost(long value) {
        throw KiltHelper.createMixinException(AnvilMenuInjection.class, "setMaximumCost");
    }

    default void kilt$handleUpdateEvent() {
        throw KiltHelper.createMixinException(AnvilMenuInjection.class, "kilt$handleUpdateEvent");
    }
}
