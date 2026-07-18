package xyz.bluspring.kilt.injections.world.inventory;

import xyz.bluspring.kilt.util.KiltHelper;

public interface AnvilMenuInjection {
    default void setCost(int value) {
        throw KiltHelper.createMixinException(AnvilMenuInjection.class, "setCost");
    }

    default void kilt$handleUpdateEvent() {
        throw KiltHelper.createMixinException(AnvilMenuInjection.class, "kilt$handleUpdateEvent");
    }
}
