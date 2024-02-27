// TRACKED HASH: 0abe6b86598c641126d968f40fad8310d1fff167
package xyz.bluspring.kilt.forgeinjects.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Screen.class)
public abstract class ScreenInject {
    @Shadow public int height;
    @Shadow public int width;
    @Shadow protected Font font;

    @Shadow @org.jetbrains.annotations.Nullable protected Minecraft minecraft;

    public Minecraft getMinecraft() {
        return this.minecraft;
    }
}