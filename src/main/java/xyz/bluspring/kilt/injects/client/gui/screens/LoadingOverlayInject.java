package xyz.bluspring.kilt.injects.client.gui.screens;

import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayInject {
    // Kilt: no point handling this
}
