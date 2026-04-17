package xyz.bluspring.kilt.injects.client.gui.components;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.gui.components.EditBoxInjection;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

@Mixin(EditBox.class)
public abstract class EditBoxInject implements EditBoxInjection {
    @Unique private boolean textShadow = true;

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"))
    private int kilt$disableTextShadowIfPossible(GuiGraphics instance, Font font, String text, int x, int y, int color, Operation<Integer> original) {
        if (this.textShadow) {
            return original.call(instance, font, text, x, y, color);
        } else {
            return instance.drawString(font, text, x, y, color, false);
        }
    }

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
    private int kilt$disableTextShadowIfPossible(GuiGraphics instance, Font font, Component text, int x, int y, int color, Operation<Integer> original) {
        if (this.textShadow) {
            return original.call(instance, font, text, x, y, color);
        } else {
            return instance.drawString(font, text, x, y, color, false);
        }
    }

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"))
    private int kilt$disableTextShadowIfPossible(GuiGraphics instance, Font font, FormattedCharSequence text, int x, int y, int color, Operation<Integer> original) {
        if (this.textShadow) {
            return original.call(instance, font, text, x, y, color);
        } else {
            return instance.drawString(font, text, x, y, color, false);
        }
    }

    @Override
    public void setTextShadow(boolean textShadow) {
        this.textShadow = textShadow;
    }

    @Override
    public boolean getTextShadow() {
        return this.textShadow;
    }
}
