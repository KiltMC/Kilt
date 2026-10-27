package xyz.bluspring.kilt.injects.client.gui.screens;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.gui.screens.MenuScreensInjection;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

@Mixin(MenuScreens.class)
public abstract class MenuScreensInject {
    @CreateStatic
    private static <T extends AbstractContainerMenu> Optional<MenuScreens.ScreenConstructor<T, ?>> getScreenFactory(MenuType<T> type) {
        return MenuScreensInjection.getScreenFactory(type);
    }

    @CreateStatic
    private static void init() {
        MenuScreensInjection.init();
    }
}
