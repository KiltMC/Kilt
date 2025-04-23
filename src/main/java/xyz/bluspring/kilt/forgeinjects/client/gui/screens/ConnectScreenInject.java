package xyz.bluspring.kilt.forgeinjects.client.gui.screens;

import net.minecraft.client.gui.screens.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenInject {
    // Kilt: no point handling this
}
