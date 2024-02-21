package xyz.bluspring.kilt.forgeinjects.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.gui.screens.inventory.tooltip.TooltipRenderUtilInjection;

@Mixin(TooltipRenderUtil.class)
public class TooltipRenderUtilInject implements TooltipRenderUtilInjection {
    @CreateStatic
    private static void renderTooltipBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, int z, int backgroundTop, int backgroundBottom, int borderTop, int borderBottom) {
        TooltipRenderUtilInjection.renderTooltipBackground(guiGraphics, x, y, width, height, z, backgroundTop, backgroundBottom, borderTop, borderBottom);
    }
}
