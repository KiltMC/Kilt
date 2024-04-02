package xyz.bluspring.kilt.mixin;

import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBindsList.class)
public interface KeyBindsListAccessor {
    @Accessor
    KeyBindsScreen getKeyBindsScreen();
}
