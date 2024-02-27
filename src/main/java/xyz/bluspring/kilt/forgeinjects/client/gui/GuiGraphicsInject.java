// TRACKED HASH: 66858683859d22896e8fbc2be116d575c4c260c3
package xyz.bluspring.kilt.forgeinjects.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.extensions.IForgeGuiGraphics;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.gui.GuiGraphicsInjection;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsInject implements GuiGraphicsInjection, IForgeGuiGraphics {
    @Shadow @Deprecated protected abstract void flushIfUnmanaged();

    @Shadow @Final private PoseStack pose;

    @Shadow @Final private MultiBufferSource.BufferSource bufferSource;

    @Shadow
    private static IntIterator slices(int target, int total) {
        return null;
    }


    @Shadow public abstract void blit(ResourceLocation atlasLocation, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight);

    @Override
    public int drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        int i = font.drawInBatch(text, x, y, color, dropShadow, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        this.flushIfUnmanaged();
        return i;
    }

    @Override
    public int drawString(Font font, @Nullable String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) {
            return 0;
        } else {
            int i = font.drawInBatch(text, x, y, color, dropShadow, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880, font.isBidirectional());
            this.flushIfUnmanaged();
            return i;
        }
    }

    @Override
    public void blitRepeating(ResourceLocation atlasLocation, int x, int y, int width, int height, int uOffset, int vOffset, int sourceWidth, int sourceHeight, int textureWidth, int textureHeight) {
        int i = x;

        int j;
        for (IntIterator intIterator = slices(width, sourceWidth); intIterator.hasNext(); i += j) {
            j = intIterator.nextInt();
            int k = (sourceWidth - j) / 2;
            int l = y;

            int m;
            for(IntIterator intIterator2 = slices(height, sourceHeight); intIterator2.hasNext(); l += m) {
                m = intIterator2.nextInt();
                int n = (sourceHeight - m) / 2;
                this.blit(atlasLocation, i, l, uOffset + k, vOffset + n, j, m, textureWidth, textureHeight);
            }
        }
    }
}