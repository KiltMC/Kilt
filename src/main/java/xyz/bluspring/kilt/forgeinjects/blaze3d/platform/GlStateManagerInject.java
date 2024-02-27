// TRACKED HASH: dfe2cf4afa6f2c9ef91cc552ca83f15c1b525aed
package xyz.bluspring.kilt.forgeinjects.blaze3d.platform;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL13;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

@Mixin(value = GlStateManager.class, remap = false)
public class GlStateManagerInject {
    @CreateStatic
    private static float lastBrightnessX = 0f;

    @CreateStatic
    private static float lastBrightnessY = 0f;

    @Inject(method = "_texParameter(IIF)V", at = @At("TAIL"), remap = false)
    private static void kilt$modifyLastBrightness(int i, int j, float f, CallbackInfo ci) {
        if (i == GL13.GL_TEXTURE1) {
            lastBrightnessX = j;
            lastBrightnessY = f;
        }
    }
}