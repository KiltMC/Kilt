package xyz.bluspring.kilt.forgeinjects.blaze3d.platform;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

@Mixin(GlStateManager.class)
public class GlStateManagerInject {
    @CreateStatic
    private static float lastBrightnessX = 0f;

    @CreateStatic
    private static float lastBrightnessY = 0f;
}
