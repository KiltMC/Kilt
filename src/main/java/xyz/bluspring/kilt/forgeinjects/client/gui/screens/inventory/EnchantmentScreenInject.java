package xyz.bluspring.kilt.forgeinjects.client.gui.screens.inventory;

import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenInject {
    // TODO: this one's incredibly difficult... we'll avoid it for now unless something highly relies on this behaviour
}
