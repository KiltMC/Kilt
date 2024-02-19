package xyz.bluspring.kilt.forgeinjects.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraftforge.client.extensions.IForgeFont;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Font.class)
public class FontInject implements IForgeFont {
    @Override
    public Font self() {
        return (Font) (Object) this;
    }
}
