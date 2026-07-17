package xyz.bluspring.kilt.injections.client.renderer;

import java.util.concurrent.atomic.AtomicReference;

import com.mojang.blaze3d.vertex.PoseStack;
import xyz.bluspring.kilt.mixin.client.renderer.ScreenEffectRendererAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;

public interface ScreenEffectRendererInjection {
    AtomicReference<Identifier> renderFluid$texture = new AtomicReference<>(null);

    static void renderFluid(Minecraft minecraft, PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture) {
        try {
            renderFluid$texture.set(texture);
            ScreenEffectRendererAccessor.kilt$callRenderWater(minecraft, poseStack, bufferSource);
        } finally {
            renderFluid$texture.set(null);
        }
    }
}
