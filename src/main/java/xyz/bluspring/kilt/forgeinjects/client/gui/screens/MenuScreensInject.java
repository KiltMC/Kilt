// TRACKED HASH: 1c741d46a32ea4ff7443ab42f7ce23558940dd93
package xyz.bluspring.kilt.forgeinjects.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.gui.screens.MenuScreensInjection;

import java.util.Optional;

@Mixin(MenuScreens.class)
public class MenuScreensInject implements MenuScreensInjection {
    @CreateStatic
    private static <T extends AbstractContainerMenu> Optional<MenuScreens.ScreenConstructor<T, ?>>  getScreenFactory(MenuType<T> menuType, Minecraft minecraft, int i, Component component) {
        return MenuScreensInjection.getScreenFactory(menuType, minecraft, i, component);
    }
}