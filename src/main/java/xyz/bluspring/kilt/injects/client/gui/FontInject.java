// TRACKED HASH: 0bb4579e7e978e3e67471f7b1b1bc23a5e23ecca
package xyz.bluspring.kilt.injects.client.gui;

import net.minecraft.client.gui.Font;
import net.neoforged.neoforge.client.extensions.IFontExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Font.class)
public class FontInject implements IFontExtension {
    @Override
    public Font self() {
        return (Font) (Object) this;
    }
}