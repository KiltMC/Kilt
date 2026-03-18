package xyz.bluspring.kilt.injects.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PanoramaRenderer;

@Mixin(PanoramaRenderer.class)
public abstract class PanoramaRendererInject {
    // Kilt: do we implement improved partial tick?

    @Inject(method = "render", at = @At("TAIL"))
    private void kilt$disableDepthTestForExtendedFarPlane(GuiGraphics guiGraphics, int width, int height, float fade, float partialTick, CallbackInfo ci) {
        RenderSystem.disableDepthTest();
    }
}
