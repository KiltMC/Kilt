package xyz.bluspring.kilt.injections.client.gui;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.gui.GuiGraphics;

public interface GuiInjection {
    default void renderSelectedItemName(GuiGraphics guiGraphics, int yShift) {
        throw KiltHelper.createMixinException(GuiInjection.class, "renderSelectedItemName");
    }

    default void initModdedOverlays() {
        throw KiltHelper.createMixinException(GuiInjection.class, "initModdedOverlays");
    }

    default int getLayerCount() {
        throw KiltHelper.createMixinException(GuiInjection.class, "getLayerCount");
    }
}
