package xyz.bluspring.kilt.injections.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public interface GuiGraphicsInjection {
    int drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow);
    int drawString(Font font, @Nullable String text, float x, float y, int color, boolean dropShadow);
    void blitRepeating(ResourceLocation atlasLocation, int x, int y, int width, int height, int uOffset, int vOffset, int sourceWidth, int sourceHeight, int textureWidth, int textureHeight);
}
