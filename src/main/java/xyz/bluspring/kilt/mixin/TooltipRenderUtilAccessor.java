package xyz.bluspring.kilt.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TooltipRenderUtil.class)
public interface TooltipRenderUtilAccessor {
    @Invoker
    static void callRenderFrameGradient(GuiGraphics guiGraphics, int x, int y, int width, int height, int z, int topColor, int bottomColor) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static void callRenderVerticalLine(GuiGraphics guiGraphics, int x, int y, int length, int z, int color) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static void callRenderVerticalLineGradient(GuiGraphics guiGraphics, int x, int y, int length, int z, int topColor, int bottomColor) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static void callRenderHorizontalLine(GuiGraphics guiGraphics, int x, int y, int length, int z, int color) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static void callRenderRectangle(GuiGraphics guiGraphics, int x, int y, int width, int height, int z, int color) {
        throw new UnsupportedOperationException();
    }
}
