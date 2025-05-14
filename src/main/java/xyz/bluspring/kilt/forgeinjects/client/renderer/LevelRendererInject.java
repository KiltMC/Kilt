// TRACKED HASH: 66d460a5ca67d57026ea83d7b1223b548ab9de3d
package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.mixinconstraints.annotations.IfModAbsent;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.renderer.LevelRendererInjection;

import java.util.concurrent.atomic.AtomicReference;

// higher priority to allow for Sodium to overwrite the method
@Mixin(value = LevelRenderer.class, priority = 1050)
public class LevelRendererInject implements LevelRendererInjection {
    @Shadow private int ticks;

    @Shadow @Nullable private Frustum capturedFrustum;
    @Shadow private Frustum cullingFrustum;
    @Shadow @Final private Minecraft minecraft;

    @Shadow private @Nullable ClientLevel level;
    @Unique private final AtomicReference<Matrix4f> kilt$projectionMatrix = new AtomicReference<>(null);

    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
    private void kilt$checkShouldRenderSnowAndRain(LightTexture lightTexture, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        if (level.effects().renderSnowAndRain(level, ticks, partialTick, lightTexture, camX, camY, camZ))
            ci.cancel();
    }

    @Inject(method = "tickRain", at = @At("HEAD"), cancellable = true)
    private void kilt$checkShouldTickRain(Camera camera, CallbackInfo ci) {
        if (level.effects().tickRain(level, ticks, camera))
            ci.cancel();
    }

    @Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=sky"))
    private void kilt$earlySetupFog(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci, @Local(ordinal = 2) boolean isFoggy, @Local(ordinal = 1) float renderDistance) {
        // TODO: Is this actually needed?
        FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, renderDistance, isFoggy, partialTick);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V", shift = At.Shift.AFTER))
    private void kilt$dispatchSkyRenderStage(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci, @Local Frustum frustum) {
        ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_SKY, (LevelRenderer) (Object) this, poseStack, projectionMatrix, this.ticks, camera, frustum);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLorg/joml/Matrix4f;)V", shift = At.Shift.AFTER, ordinal = 0))
    private void kilt$fixBlurFlicker(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        // Kilt: we don't actually experience this bug, but just in case I guess?
        this.minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, this.minecraft.options.mipmapLevels().get() > 0);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLorg/joml/Matrix4f;)V", shift = At.Shift.AFTER, ordinal = 1))
    private void kilt$resetBlurFlickerFix(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        // Kilt: we don't actually experience this bug, but just in case I guess?
        this.minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
    }

    // TODO: this is still incomplete, we need to finish this.

    @Override
    public Matrix4f kilt$getProjectionMatrix() {
        return kilt$projectionMatrix.get();
    }

    @IfModLoaded("sodium")
    @Inject(method = "renderChunkLayer", at = @At("HEAD"))
    public void kilt$sodiumStoreProjectionMatrix(RenderType renderType, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix, CallbackInfo ci) {
        // Sodium doesn't give the projection matrix to itself, so we should store it.
        // If there's a better way of doing this, please PR.
        this.kilt$projectionMatrix.set(projectionMatrix);
    }

    @IfModAbsent("sodium")
    @Inject(method = "renderChunkLayer", at = @At(value = "TAIL", shift = At.Shift.BY, by = -1))
    public void kilt$dispatchRenderEventBasedOnType(RenderType renderType, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix, CallbackInfo ci) {
        var stage = RenderLevelStageEvent.Stage.fromRenderType(renderType);

        if (stage != null) {
            MinecraftForge.EVENT_BUS.post(new RenderLevelStageEvent(stage, (LevelRenderer) (Object) this, poseStack, projectionMatrix, this.ticks, this.minecraft.getPartialTick(), this.minecraft.gameRenderer.getMainCamera(), this.capturedFrustum != null ? this.capturedFrustum : this.cullingFrustum));
        }
    }
}