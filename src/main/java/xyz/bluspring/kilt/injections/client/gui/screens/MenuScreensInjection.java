package xyz.bluspring.kilt.injections.client.gui.screens;

import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import xyz.bluspring.kilt.mixin.MenuScreensAccessor;

public interface MenuScreensInjection {
    static void init() {
        ModLoader.postEvent(new RegisterMenuScreensEvent(MenuScreensAccessor.kilt$getScreens()));
    }
}
