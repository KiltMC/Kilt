// TRACKED HASH: a9103d56b3de8410d00b20f87a25ee9581cf7658
package xyz.bluspring.kilt.forgeinjects.client.gui.components;

import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.gui.AbstractSelectionListInjection;

@Mixin(AbstractSelectionList.class)
public class AbstractSelectionListInject implements AbstractSelectionListInjection {
    @Shadow protected int width;

    @Shadow protected int height;

    @Shadow protected int y0;

    @Shadow protected int y1;

    @Shadow protected int x0;

    @Shadow protected int x1;

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getTop() {
        return this.y0;
    }

    @Override
    public int getBottom() {
        return this.y1;
    }

    @Override
    public int getLeft() {
        return this.x0;
    }

    @Override
    public int getRight() {
        return this.x1;
    }
}