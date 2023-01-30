package xyz.bluspring.kilt.injections.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import xyz.bluspring.kilt.mixin.ScreenEffectRendererAccessor;

import java.util.concurrent.atomic.AtomicReference;

public interface ScreenEffectRendererInjection {
    AtomicReference<ResourceLocation> currentTexture = new AtomicReference<>();

    static void renderFluid(Minecraft mc, PoseStack poseStack, ResourceLocation texture) {
        currentTexture.set(texture);
        ScreenEffectRendererAccessor.callRenderWater(mc, poseStack);
        currentTexture.set(ScreenEffectRendererAccessor.getUnderwaterLocation());
    }
}
