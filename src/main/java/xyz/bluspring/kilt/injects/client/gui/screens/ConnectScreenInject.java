package xyz.bluspring.kilt.injects.client.gui.screens;

import net.minecraft.client.gui.screens.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenInject {
    // Kilt: no point handling this
}
