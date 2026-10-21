package xyz.bluspring.kilt.injections.client.gui;

import xyz.bluspring.kilt.util.KiltHelper;

public interface GuiInjection {
    default void initModdedOverlays() {
        throw KiltHelper.createMixinException(GuiInjection.class, "initModdedOverlays");
    }
}
