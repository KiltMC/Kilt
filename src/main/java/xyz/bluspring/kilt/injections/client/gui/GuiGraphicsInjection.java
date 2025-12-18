package xyz.bluspring.kilt.injections.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;
import java.util.Optional;

public interface GuiGraphicsInjection {
    default int drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        throw KiltHelper.createMixinException(GuiGraphicsInjection.class, "drawString");
    }

    default int drawString(Font font, @Nullable String text, float x, float y, int color, boolean dropShadow)  {
        throw KiltHelper.createMixinException(GuiGraphicsInjection.class, "drawString");
    }

    default void blitRepeating(ResourceLocation atlasLocation, int x, int y, int width, int height, int uOffset, int vOffset, int sourceWidth, int sourceHeight, int textureWidth, int textureHeight) {
        throw KiltHelper.createMixinException(GuiGraphicsInjection.class, "blitRepeating");
    }

    default void renderComponentTooltip(Font font, List<? extends FormattedText> tooltips, int mouseX, int mouseY, ItemStack stack) {
        throw KiltHelper.createMixinException(GuiGraphicsInjection.class, "renderComponentTooltip");
    }

    default void renderTooltip(Font font, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, ItemStack stack, int mouseX, int mouseY) {
        throw KiltHelper.createMixinException(GuiGraphicsInjection.class, "renderTooltip");
    }

    default ItemStack kilt$getTooltipStack() {
        throw KiltHelper.createMixinException(GuiGraphicsInjection.class, "kilt$getTooltipStack");
    }

    default void kilt$setTooltipStack(ItemStack stack) {
        throw KiltHelper.createMixinException(GuiGraphicsInjection.class, "kilt$setTooltipStack");
    }
}
