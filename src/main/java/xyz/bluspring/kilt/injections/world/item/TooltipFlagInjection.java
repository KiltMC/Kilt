package xyz.bluspring.kilt.injections.world.item;

import xyz.bluspring.kilt.util.KiltHelper;

public interface TooltipFlagInjection {
    default boolean hasControlDown() {
        throw KiltHelper.createMixinException(TooltipFlagInjection.class, "hasControlDown");
    }

    default boolean hasShiftDown() {
        throw KiltHelper.createMixinException(TooltipFlagInjection.class, "hasShiftDown");
    }

    default boolean hasAltDown() {
        throw KiltHelper.createMixinException(TooltipFlagInjection.class, "hasAltDown");
    }
}
