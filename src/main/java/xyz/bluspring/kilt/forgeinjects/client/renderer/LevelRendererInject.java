// TRACKED HASH: 66d460a5ca67d57026ea83d7b1223b548ab9de3d
package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moulberry.mixinconstraints.annotations.IfModAbsent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.particle.ParticleEngineInjection;
import xyz.bluspring.kilt.injections.client.renderer.LevelRendererInjection;
import xyz.bluspring.kilt.injections.client.renderer.block.BlockRenderDispatcherInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;

// higher priority to allow for Sodium to overwrite the method
@Mixin(value = LevelRenderer.class, priority = 1050)
public abstract class LevelRendererInject implements LevelRendererInjection {
    @Shadow private int ticks;

    @Shadow @Nullable private Frustum capturedFrustum;
    @Shadow private Frustum cullingFrustum;
    @Shadow @Final private Minecraft minecraft;

    @Shadow private @Nullable ClientLevel level;

    @Shadow public abstract void playStreamingMusic(@Nullable SoundEvent soundEvent, BlockPos pos);
    @Shadow public abstract boolean shouldShowEntityOutlines();

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

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
    private void kilt$allowCustomOutlineRendering(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci, @Local MultiBufferSource.BufferSource bufferSource, @Local MultiBufferSource multiBufferSource, @Local Entity entity, @Local(ordinal = 3) LocalBooleanRef flag) {
        if (multiBufferSource == bufferSource) {
            if (this.shouldShowEntityOutlines() && entity.hasCustomOutlineRendering(this.minecraft.player)) {
                flag.set(true);
            }
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V", ordinal = 3, shift = At.Shift.AFTER))
    private void kilt$dispatchAfterEntitiesStage(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci, @Local Frustum frustum) {
        ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_ENTITIES, (LevelRenderer) (Object) this, poseStack, projectionMatrix, this.ticks, camera, frustum);
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;getRenderableBlockEntities()Ljava/util/List;"))
    private List<BlockEntity> kilt$removeInvisibleBlockEntities(List<BlockEntity> original, @Local Frustum frustum) {
        return original.stream().filter(e -> frustum.isVisible(e.getRenderBoundingBox())).toList();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
    private void kilt$allowCustomOutlineRendering2(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci, @Local(ordinal = 3) LocalBooleanRef flag, @Local BlockEntity blockEntity) {
        if (this.shouldShowEntityOutlines() && blockEntity.hasCustomOutlineRendering(this.minecraft.player)) {
            flag.set(true);
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=destroyProgress"))
    private void kilt$dispatchPostBlockEntitiesEvent(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci, @Local Frustum frustum) {
        ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES, (LevelRenderer) (Object) this, poseStack, projectionMatrix, this.ticks, camera, frustum);
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    private void kilt$useModelDataRenderIfPossible(BlockRenderDispatcher instance, BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack poseStack, VertexConsumer consumer, Operation<Void> original) {
        var modelData = level.getModelDataManager().getAt(pos);

        if (modelData == ModelData.EMPTY || modelData == null)
            original.call(instance, state, pos, level, poseStack, consumer);
        else
            ((BlockRenderDispatcherInjection) instance).renderBreakingTexture(state, pos, level, poseStack, consumer, modelData);
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean kilt$checkDrawHighlightEvent(boolean original, @Local(argsOnly = true) Camera camera, @Local HitResult hitResult, @Local(argsOnly = true) float partialTick, @Local(argsOnly = true) PoseStack poseStack, @Local MultiBufferSource.BufferSource bufferSource) {
        return ForgeHooksClient.onDrawHighlight((LevelRenderer) (Object) this, camera, hitResult, partialTick, poseStack, bufferSource) || original;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V"))
    private void kilt$callDrawHighlightEvent(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci, @Local MultiBufferSource.BufferSource bufferSource) {
        HitResult hitResult = this.minecraft.hitResult;
        if (!renderBlockOutline && hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            ForgeHooksClient.onDrawHighlight((LevelRenderer) (Object) this, camera, hitResult, partialTick, poseStack, bufferSource);
        }
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V", ordinal = 0))
    private void kilt$storeFrustumAndDispatchAfterParticles(ParticleEngine instance, PoseStack poseStack, MultiBufferSource.BufferSource buffer, LightTexture lightTexture, Camera activeRenderInfo, float partialTicks, Operation<Void> original, @Local Frustum frustum, @Local(argsOnly = true) Matrix4f projectionMatrix) {
        instance.kilt$setClippingHelper(frustum);
        original.call(instance, poseStack, buffer, lightTexture, activeRenderInfo, partialTicks);
        instance.kilt$setClippingHelper(null);

        ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_PARTICLES, (LevelRenderer) (Object) this, poseStack, projectionMatrix, this.ticks, activeRenderInfo, frustum);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V", ordinal = 1))
    private void kilt$kilt$dispatchAfterParticles(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci, @Local Frustum frustum) {
        ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_PARTICLES, (LevelRenderer) (Object) this, poseStack, projectionMatrix, this.ticks, camera, frustum);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", shift = At.Shift.AFTER))
    private void kilt$dispatchAfterWeatherStage(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci, @Local Frustum frustum) {
        ForgeHooksClient.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_WEATHER, (LevelRenderer) (Object) this, poseStack, projectionMatrix, this.ticks, camera, frustum);
    }

    @IfModAbsent("sodium")
    @Inject(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;clearRenderState()V"))
    private void kilt$dispatchCurrentTypeStage(RenderType renderType, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix, CallbackInfo ci) {
        ForgeHooksClient.dispatchRenderStage(renderType, (LevelRenderer) (Object) this, poseStack, projectionMatrix, this.ticks, this.minecraft.gameRenderer.getMainCamera(), this.getFrustum());
    }

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void kilt$checkRenderSkyEffects(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        if (this.level.effects().renderSky(this.level, this.ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, skyFogSetup))
            ci.cancel();
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void kilt$checkRenderCloudEffects(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        if (this.level.effects().renderClouds(this.level, this.ticks, partialTick, poseStack, camX, camY, camZ, projectionMatrix))
            ci.cancel();
    }

    // Kilt: we don't need to handle off-thread terrain setup

    @Override
    public Frustum getFrustum() {
        return this.capturedFrustum != null ? this.capturedFrustum : this.cullingFrustum;
    }

    @Override
    public int getTicks() {
        return ticks;
    }

    private RecordItem kilt$currentRecordItem;

    @Override
    public void playStreamingMusic(@Nullable SoundEvent soundEvent, BlockPos pos, @Nullable RecordItem musicDiscItem) {
        this.kilt$currentRecordItem = musicDiscItem;
        this.playStreamingMusic(soundEvent, pos);
        this.kilt$currentRecordItem = null;
    }

    @ModifyExpressionValue(method = "playStreamingMusic", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/RecordItem;getBySound(Lnet/minecraft/sounds/SoundEvent;)Lnet/minecraft/world/item/RecordItem;"))
    private RecordItem kilt$useCustomRecordItem(RecordItem original) {
        if (this.kilt$currentRecordItem != null)
            return this.kilt$currentRecordItem;

        return original;
    }

    @WrapOperation(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;playStreamingMusic(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/core/BlockPos;)V", ordinal = 0))
    private void kilt$storeCurrentRecordItem(LevelRenderer instance, SoundEvent soundEvent, BlockPos pos, Operation<Void> original, @Local(ordinal = 1, argsOnly = true) int data) {
        this.kilt$currentRecordItem = (RecordItem) Item.byId(data);
        original.call(instance, soundEvent, pos);
        this.kilt$currentRecordItem = null;
    }

    @WrapOperation(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType kilt$tryUseForgeSoundType(BlockState instance, Operation<SoundType> original, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), IForgeBlock.class, "getSoundType", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
            return instance.getSoundType(this.level, pos, null);
        }

        return original.call(instance);
    }

    @WrapOperation(method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private static int kilt$tryUseForgeLightEmission(BlockState instance, Operation<Integer> original, @Local(argsOnly = true) BlockAndTintGetter level, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), IForgeBlock.class, "getLightEmission", BlockState.class, BlockAndTintGetter.class, BlockPos.class)) {
            return instance.getLightEmission(level, pos);
        }

        return original.call(instance);
    }
}