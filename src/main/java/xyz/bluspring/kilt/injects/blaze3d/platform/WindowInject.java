package xyz.bluspring.kilt.injects.blaze3d.platform;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Window.class)
public abstract class WindowInject {
    // Kilt: this is for ELS, we don't do ELS here
}
