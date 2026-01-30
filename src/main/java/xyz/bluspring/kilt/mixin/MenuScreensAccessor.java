package xyz.bluspring.kilt.mixin;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MenuScreens.class)
public interface MenuScreensAccessor {
    @Accessor("SCREENS")
    static Map<MenuType<?>, MenuScreens.ScreenConstructor<?, ?>> kilt$getScreens() {
        throw new UnsupportedOperationException();
    }

    @Accessor("LOGGER")
    static Logger kilt$getLogger() {
        throw new UnsupportedOperationException();
    }
}
