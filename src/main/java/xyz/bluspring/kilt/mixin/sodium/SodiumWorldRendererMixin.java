package xyz.bluspring.kilt.mixin.sodium;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.render.LevelRendererInjection;
import xyz.bluspring.kilt.mixin.LevelRendererAccessor;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class SodiumWorldRendererMixin {
    @Inject(method = "drawChunkLayer", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPass;endDrawing()V", shift = At.Shift.BEFORE, remap = false), remap = false)
    public void kilt$sodiumDispatchRenderEventBasedOnType(RenderType renderLayer, PoseStack matrixStack, double x, double y, double z, CallbackInfo ci) {
        var stage = RenderLevelStageEvent.Stage.fromRenderType(renderLayer);

        if (stage != null) {
            var mc = Minecraft.getInstance();
            var levelRenderer = (LevelRendererAccessor) mc.levelRenderer;
            var projectionMatrix = ((LevelRendererInjection) mc.levelRenderer).getProjectionMatrix();

            // it might be possible for this to be null, so let's just make sure that never happens.
            if (projectionMatrix == null)
                return;

            MinecraftForge.EVENT_BUS.post(new RenderLevelStageEvent(stage, mc.levelRenderer, matrixStack, projectionMatrix, levelRenderer.getTicks(), mc.getPartialTick(), mc.gameRenderer.getMainCamera(), levelRenderer.getCapturedFrustum() != null ? levelRenderer.getCapturedFrustum() : levelRenderer.getCullingFrustum()));
        }
    }
}
