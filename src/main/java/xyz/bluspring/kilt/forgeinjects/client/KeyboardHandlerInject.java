package xyz.bluspring.kilt.forgeinjects.client;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerInject {
    // Kilt: Handled by Architectury, in theory.
}
