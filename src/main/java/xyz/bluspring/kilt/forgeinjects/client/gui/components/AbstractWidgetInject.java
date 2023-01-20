package xyz.bluspring.kilt.forgeinjects.client.gui.components;

import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.gui.AbstractWidgetInjection;
import xyz.bluspring.kilt.remaps.client.gui.components.AbstractWidgetRemap;

@Mixin(AbstractWidget.class)
public class AbstractWidgetInject implements AbstractWidgetInjection {
    @Shadow public boolean active;
    protected int packedFGColor = AbstractWidgetRemap.UNSET_FG_COLOR;

    @Override
    public int getFGColor() {
        if (packedFGColor != AbstractWidgetRemap.UNSET_FG_COLOR)
            return packedFGColor;

        return this.active ? 16777215 : 10526880; // white : light grey
    }

    @Override
    public void setFGColor(int color) {
        packedFGColor = color;
    }

    @Override
    public void clearFGColor() {
        packedFGColor = AbstractWidgetRemap.UNSET_FG_COLOR;
    }
}
