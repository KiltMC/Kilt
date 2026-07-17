package xyz.bluspring.kilt.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;

@Mixin(ScreenEffectRenderer.class)
public interface ScreenEffectRendererAccessor {
    @Invoker("renderWater")
    static void kilt$callRenderWater(final Minecraft minecraft, final PoseStack poseStack, final MultiBufferSource bufferSource) {
        throw new UnsupportedOperationException();
    }
}
