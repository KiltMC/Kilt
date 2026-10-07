// TRACKED HASH: 5e539bec12a4a4d92962df738afd4a7595db2eeb
package xyz.bluspring.kilt.injects.client.gui.components;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.client.extensions.IAbstractWidgetExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.gui.components.AbstractWidgetInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.gui.components.AbstractWidget;

@Mixin(AbstractWidget.class)
public class AbstractWidgetInject implements AbstractWidgetInjection, IAbstractWidgetExtension {
    @Shadow public boolean active;
    @Shadow protected int height;

    protected int packedFGColor = AbstractWidgetInjection.UNSET_FG_COLOR;

    @WrapOperation(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractWidget;onClick(DD)V"))
    private void kilt$handleNeoOnClick(AbstractWidget instance, double mouseX, double mouseY, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) int button) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IAbstractWidgetExtension.class, "onClick", double.class, double.class, int.class)) {
            instance.onClick(mouseX, mouseY, button);
        } else {
            original.call(instance, mouseX, mouseY);
        }
    }

    @CreateStatic
    private static final int UNSET_FG_COLOR = AbstractWidgetInjection.UNSET_FG_COLOR;

    @Override
    public int getFGColor() {
        if (packedFGColor != AbstractWidgetInjection.UNSET_FG_COLOR)
            return packedFGColor;

        return this.active ? 16777215 : 10526880; // white : light grey
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
