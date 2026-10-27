package xyz.bluspring.kilt.injections.client.gui.screens;

import java.util.Optional;

import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import xyz.bluspring.kilt.mixin.MenuScreensAccessor;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface MenuScreensInjection {
    static void init() {
        ModLoader.postEvent(new RegisterMenuScreensEvent(MenuScreensAccessor.kilt$getScreens()));
    }

    static <T extends AbstractContainerMenu> Optional<MenuScreens.ScreenConstructor<T, ?>> getScreenFactory(MenuType<T> menuType) {
        var screenConstructor = MenuScreens.getConstructor(menuType);

        if (screenConstructor == null) {
            MenuScreensAccessor.kilt$getLogger().warn("Failed to create screen for menu type: {}", BuiltInRegistries.MENU.getKey(menuType));
        } else {
            return Optional.of(screenConstructor);
        }

        return Optional.empty();
    }
}
