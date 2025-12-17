package xyz.bluspring.kilt.injections.client.gui.screens;

import net.minecraft.client.gui.components.events.GuiEventListener;
import xyz.bluspring.kilt.util.KiltHelper;

public interface ScreenInjection {
    default void kilt$addEventWidget(GuiEventListener b) {
        throw new IllegalStateException();
    }
}
