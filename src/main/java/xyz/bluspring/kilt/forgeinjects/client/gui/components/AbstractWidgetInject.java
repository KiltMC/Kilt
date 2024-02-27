// TRACKED HASH: 5e539bec12a4a4d92962df738afd4a7595db2eeb
package xyz.bluspring.kilt.forgeinjects.client.gui.components;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraftforge.client.extensions.IAbstractWidgetExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.gui.AbstractWidgetInjection;

@Mixin(AbstractWidget.class)
public class AbstractWidgetInject implements AbstractWidgetInjection, IAbstractWidgetExtension {
    @Shadow public boolean active;
    @Shadow protected int height;

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractWidget;onClick(DD)V"))
    private void kilt$useForgeClick(AbstractWidget instance, double mouseX, double mouseY, double mx, double my, int button) {
        this.onClick(mouseX, mouseY, button);
    }

    protected int packedFGColor = AbstractWidgetInjection.UNSET_FG_COLOR;

    @CreateStatic
    private static int UNSET_FG_COLOR = AbstractWidgetInjection.UNSET_FG_COLOR;

    @Override
    public int getFGColor() {
        if (packedFGColor != AbstractWidgetInjection.UNSET_FG_COLOR)
            return packedFGColor;

        return this.active ? 16777215 : 10526880; // white : light grey
    }

    @Override
    public void setHeight(int value) {
        this.height = value;
    }

    @Override
    public void setFGColor(int color) {
        packedFGColor = color;
    }

    @Override
    public void clearFGColor() {
        packedFGColor = AbstractWidgetInjection.UNSET_FG_COLOR;
    }
}