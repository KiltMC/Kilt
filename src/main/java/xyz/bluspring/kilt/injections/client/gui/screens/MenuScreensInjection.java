package xyz.bluspring.kilt.injections.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public interface MenuScreensInjection {
    Logger logger = LogManager.getLogger();

    // Copied from Minecraft, with Forge's patches added on.
    // Apparently literally none of the params are actually used by Forge. What the hell.
    static <T extends AbstractContainerMenu> Optional<MenuScreens.ScreenConstructor<T, ?>> getScreenFactory(MenuType<T> menuType, Minecraft minecraft, int i, Component component) {
        if (menuType == null) {
            logger.warn("Trying to open invalid screen with name: {}", component.getString());
        } else {
            var screenConstructor = MenuScreens.getConstructor(menuType);
            if (screenConstructor == null) {
                logger.warn("Failed to create screen for menu type: {}", Registry.MENU.getKey(menuType));
            } else {
                return Optional.of(screenConstructor);
            }
        }

        return Optional.empty();
    }
}
