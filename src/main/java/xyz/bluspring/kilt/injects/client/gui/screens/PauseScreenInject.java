package xyz.bluspring.kilt.injects.client.gui.screens;

import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PauseScreen.class)
public abstract class PauseScreenInject {
    // Kilt: handled by ModMenu
}
