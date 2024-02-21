package xyz.bluspring.kilt.injections.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.GuiGraphics;
import xyz.bluspring.kilt.mixin.TooltipRenderUtilAccessor;

public interface TooltipRenderUtilInjection {
    static void renderTooltipBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, int z, int backgroundTop, int backgroundBottom, int borderTop, int borderBottom) {
        int i = x - 3;
        int j = y - 3;
        int k = width + 3 + 3;
        int l = height + 3 + 3;

        TooltipRenderUtilAccessor.callRenderHorizontalLine(guiGraphics, i, j - 1, k, z, backgroundTop);
        TooltipRenderUtilAccessor.callRenderHorizontalLine(guiGraphics, i, j + l, k, z, backgroundBottom);
        renderRectangle(guiGraphics, i, j, k, l, z, backgroundTop, backgroundBottom);
        TooltipRenderUtilAccessor.callRenderVerticalLineGradient(guiGraphics, i - 1, j, l, z, backgroundTop, backgroundBottom);
        TooltipRenderUtilAccessor.callRenderVerticalLineGradient(guiGraphics, i + k, j, l, z, backgroundTop, backgroundBottom);
        TooltipRenderUtilAccessor.callRenderFrameGradient(guiGraphics, i, j + 1, k, l, z, borderTop, borderBottom);
    }

    private static void renderRectangle(GuiGraphics guiGraphics, int x, int y, int width, int height, int z, int colorFrom, int colorTo) {
        guiGraphics.fillGradient(x, y, x + width, y + height, colorFrom, colorTo);
    }
}
