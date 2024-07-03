package xyz.bluspring.kilt.mixin.compat.sodium;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.renderer.LevelRendererInjection;
import xyz.bluspring.kilt.mixin.LevelRendererAccessor;

@IfModLoaded("sodium")
@Mixin(value = SodiumWorldRenderer.class, remap = false)
@Pseudo
public class SodiumWorldRendererMixin {
    @IfModLoaded(value = "sodium", maxVersion = "0.5.8")
    @Dynamic
    @Inject(method = "drawChunkLayer", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSectionManager;renderLayer(Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderMatrices;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;DDD)V", shift = At.Shift.BEFORE, remap = false), remap = false)
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

    @IfModLoaded(value = "sodium", minVersion = "0.5.9")
    @Dynamic
    @Inject(method = "drawChunkLayer", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSectionManager;renderLayer(Lme/jellysquid/mods/sodium/client/render/chunk/ChunkRenderMatrices;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;DDD)V", shift = At.Shift.BEFORE, remap = false), remap = false)
    public void kilt$sodiumDispatchRenderEventBasedOnType(RenderType renderLayer, ChunkRenderMatrices matrices, double x, double y, double z, CallbackInfo ci) {
        var stage = RenderLevelStageEvent.Stage.fromRenderType(renderLayer);

        if (stage != null) {
            var mc = Minecraft.getInstance();
            var levelRenderer = (LevelRendererAccessor) mc.levelRenderer;
            var projectionMatrix = new Matrix4f(matrices.projection());
            var poseStack = new PoseStack();
            poseStack.mulPoseMatrix(new Matrix4f(matrices.modelView()));

            MinecraftForge.EVENT_BUS.post(new RenderLevelStageEvent(stage, mc.levelRenderer, poseStack, projectionMatrix, levelRenderer.getTicks(), mc.getPartialTick(), mc.gameRenderer.getMainCamera(), levelRenderer.getCapturedFrustum() != null ? levelRenderer.getCapturedFrustum() : levelRenderer.getCullingFrustum()));
        }
    }
}
