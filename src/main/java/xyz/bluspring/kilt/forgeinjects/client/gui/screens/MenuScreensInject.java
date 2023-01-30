package xyz.bluspring.kilt.forgeinjects.client.gui.screens;

import net.minecraft.client.gui.screens.MenuScreens;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.client.gui.screens.MenuScreensInjection;

@Mixin(MenuScreens.class)
public class MenuScreensInject implements MenuScreensInjection {
}
