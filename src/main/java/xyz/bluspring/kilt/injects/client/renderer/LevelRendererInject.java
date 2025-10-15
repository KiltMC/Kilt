// TRACKED HASH: 66d460a5ca67d57026ea83d7b1223b548ab9de3d
package xyz.bluspring.kilt.injects.client.renderer;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moulberry.mixinconstraints.annotations.IfModAbsent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.renderer.LevelRendererInjection;
import xyz.bluspring.kilt.injections.client.renderer.block.BlockRenderDispatcherInjection;
import xyz.bluspring.kilt.util.IteratorWrapper;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

// higher priority to allow for Sodium to overwrite the method
@Mixin(value = LevelRenderer.class, priority = 1050)
@Implements(@Interface(iface = LevelRendererInjection.class, prefix = "kilt$i$"))
public abstract class LevelRendererInject {
    @Shadow private int ticks;
    @Shadow @Nullable private Frustum capturedFrustum;
    @Shadow private Frustum cullingFrustum;
    @Shadow @Final private Minecraft minecraft;
    @Shadow private @Nullable ClientLevel level;
    @Shadow public abstract boolean shouldShowEntityOutlines();
    @Shadow @Final private BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    @Shadow @Final private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;
    @Shadow @Final private Set<BlockEntity> globalBlockEntities;

    @Unique
    private boolean outlineEffectRequested = false;

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
    private void kilt$earlySetupFog(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local(ordinal = 2) boolean isFoggy, @Local(ordinal = 1) float renderDistance, @Local(ordinal = 0) float partialTick) {
        // TODO: Is this actually needed?
        FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, renderDistance, isFoggy, partialTick);
    }

    @Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=fog"))
    private void kilt$dispatchSkyRenderStage(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local Frustum frustum) {
        ClientHooks.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_SKY, (LevelRenderer) (Object) this, null, frustumMatrix, projectionMatrix, this.ticks, camera, frustum);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSectionLayer(Lnet/minecraft/client/renderer/RenderType;DDDLorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void kilt$fixBlurFlicker(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        // Kilt: we don't actually experience this bug, but just in case I guess?
        this.minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, this.minecraft.options.mipmapLevels().get() > 0);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSectionLayer(Lnet/minecraft/client/renderer/RenderType;DDDLorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER, ordinal = 1))
    private void kilt$resetBlurFlickerFix(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        // Kilt: we don't actually experience this bug, but just in case I guess?
        this.minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
    }

    @Definition(id = "camera", local = @Local(type = Camera.class, argsOnly = true))
    @Definition(id = "getEntity", method = "Lnet/minecraft/client/Camera;getEntity()Lnet/minecraft/world/entity/Entity;")
    @Expression("camera.getEntity() == ?")
    @ModifyExpressionValue(method = "renderLevel", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$renderLocalPlayerWhenNotCamera(boolean original, @Local Entity entity) {
        return original || (entity == minecraft.player && !minecraft.player.isSpectator());
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
    private void kilt$allowCustomOutlineRendering(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local MultiBufferSource.BufferSource bufferSource, @Local MultiBufferSource multiBufferSource, @Local Entity entity, @Local(ordinal = 3) LocalBooleanRef flag) {
        if (multiBufferSource == bufferSource) {
            if (this.shouldShowEntityOutlines() && entity.hasCustomOutlineRendering(this.minecraft.player)) {
                flag.set(true);
            }
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=blockentities"))
    private void kilt$dispatchAfterEntitiesStage(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local Frustum frustum, @Local PoseStack poseStack) {
        ClientHooks.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_ENTITIES, (LevelRenderer) (Object) this, poseStack, frustumMatrix, projectionMatrix, this.ticks, camera, frustum);
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0))
    private <E> Iterator<E> kilt$removeInvisibleBlockEntities(Iterator<E> original, @Local Frustum frustum) {
        var renderDispatcher = blockEntityRenderDispatcher;
        return (Iterator<E>) new IteratorWrapper<BlockEntity>((Iterator<? extends BlockEntity>) original, (blockEntity) ->
            ClientHooks.isBlockEntityRendererVisible(renderDispatcher, blockEntity, frustum) ? blockEntity : null
        );
    }

    // Kilt: works for both!
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
    private void kilt$allowCustomOutlineRendering2(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local(ordinal = 3) LocalBooleanRef flag, @Local BlockEntity blockEntity) {
        if (this.shouldShowEntityOutlines() && blockEntity.hasCustomOutlineRendering(this.minecraft.player)) {
            flag.set(true);
        }
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"))
    private <E> Iterator<E> kilt$removeInvisibleGlobalBlockEntities(Iterator<E> original, @Local Frustum frustum) {
        var renderDispatcher = blockEntityRenderDispatcher;
        return (Iterator<E>) new IteratorWrapper<BlockEntity>((Iterator<? extends BlockEntity>) original, (blockEntity) ->
            ClientHooks.isBlockEntityRendererVisible(renderDispatcher, blockEntity, frustum) ? blockEntity : null
        );
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V", shift = At.Shift.AFTER))
    private void kilt$handleOutlineEffectsForOtherEntities(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local(ordinal = 3) LocalBooleanRef flag) {
        if (this.outlineEffectRequested) {
            flag.set(flag.get() | this.shouldShowEntityOutlines());
            this.outlineEffectRequested = false;
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=destroyProgress"))
    private void kilt$dispatchPostBlockEntitiesEvent(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local Frustum frustum, @Local PoseStack poseStack) {
        ClientHooks.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES, (LevelRenderer) (Object) this, poseStack, frustumMatrix, projectionMatrix, this.ticks, camera, frustum);
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    private void kilt$useModelDataRenderIfPossible(BlockRenderDispatcher instance, BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack poseStack, VertexConsumer consumer, Operation<Void> original) {
        var modelData = level.getModelData(pos);

        if (modelData == ModelData.EMPTY || modelData == null)
            original.call(instance, state, pos, level, poseStack, consumer);
        else
            instance.renderBreakingTexture(state, pos, level, poseStack, consumer, modelData);
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean kilt$checkDrawHighlightEvent(boolean original, @Local(argsOnly = true) Camera camera, @Local HitResult hitResult, @Local DeltaTracker deltaTracker, @Local PoseStack poseStack, @Local MultiBufferSource.BufferSource bufferSource) {
        return ClientHooks.onDrawHighlight((LevelRenderer) (Object) this, camera, hitResult, deltaTracker, poseStack, bufferSource) || original;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V"))
    private void kilt$callDrawHighlightEvent(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local MultiBufferSource.BufferSource bufferSource, @Local PoseStack poseStack) {
        HitResult hitResult = this.minecraft.hitResult;
        if (!renderBlockOutline && hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            ClientHooks.onDrawHighlight((LevelRenderer) (Object) this, camera, hitResult, deltaTracker, poseStack, bufferSource);
        }
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V"))
    private void kilt$storeFrustumAndDispatchAfterParticles(ParticleEngine instance, LightTexture lightTexture, Camera camera, float f, Operation<Void> original, @Local Frustum frustum, @Local PoseStack poseStack, @Local(argsOnly = true, ordinal = 0) Matrix4f frustumMatrix, @Local(argsOnly = true, ordinal = 1) Matrix4f projectionMatrix) {
        instance.kilt$setClippingHelper(frustum);
        original.call(instance, lightTexture, camera, f);
        instance.kilt$setClippingHelper(null);

        ClientHooks.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_PARTICLES, (LevelRenderer) (Object) this, poseStack, frustumMatrix, projectionMatrix, this.ticks, camera, frustum);
    }

    // Kilt: we're not doing Neo's particle rendering patch.

    // applies for both
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", shift = At.Shift.AFTER))
    private void kilt$dispatchAfterWeatherStage(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local Frustum frustum, @Local PoseStack poseStack) {
        ClientHooks.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_WEATHER, (LevelRenderer) (Object) this, poseStack, frustumMatrix, projectionMatrix, this.ticks, camera, frustum);
    }

    @IfModAbsent("sodium")
    @Inject(method = "renderSectionLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;clearRenderState()V"))
    private void kilt$dispatchCurrentTypeStage(RenderType renderType, double x, double y, double z, Matrix4f frustrumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        ClientHooks.dispatchRenderStage(renderType, (LevelRenderer) (Object) this, frustrumMatrix, projectionMatrix, this.ticks, this.minecraft.gameRenderer.getMainCamera(), this.kilt$i$getFrustum());
    }

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void kilt$checkRenderSkyEffects(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        if (this.level.effects().renderSky(this.level, this.ticks, partialTick, frustumMatrix, camera, projectionMatrix, isFoggy, skyFogSetup))
            ci.cancel();
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void kilt$checkRenderCloudEffects(PoseStack poseStack, Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        if (this.level.effects().renderClouds(this.level, this.ticks, partialTick, poseStack, camX, camY, camZ, frustumMatrix, projectionMatrix))
            ci.cancel();
    }

    // Kilt: we don't need to handle off-thread terrain setup

    // methods added in by Neo.
    // prefixed with kilt$i$ to "soft-implement" in the event another mod is using this same method signature,
    // and using @Intrinsic(displace = true) to try to ensure that our behaviour would still be called.
    // @Intrinsic alone would be used to try to prioritize the other mod's behaviour, typically for instances
    // where either a value is returned or if it's possible for things to potentially run twice when undesired.
    @Intrinsic
    public Frustum kilt$i$getFrustum() {
        return this.capturedFrustum != null ? this.capturedFrustum : this.cullingFrustum;
    }

    @Intrinsic
    public int kilt$i$getTicks() {
        return ticks;
    }

    @Intrinsic
    public void kilt$i$iterateVisibleBlockEntities(Consumer<BlockEntity> blockEntityConsumer) {
        for (SectionRenderDispatcher.RenderSection chunkInfo : this.visibleSections) {
            chunkInfo.getCompiled().getRenderableBlockEntities().forEach(blockEntityConsumer);
        }

        synchronized (this.globalBlockEntities) {
            this.globalBlockEntities.forEach(blockEntityConsumer);
        }
    }

    @Intrinsic(displace = true)
    public void kilt$i$requestOutlineEffect() {
        this.outlineEffectRequested = true;
    }

    @WrapOperation(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType kilt$tryUseForgeSoundType(BlockState instance, Operation<SoundType> original, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), Block.class, "getSoundType", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
            return instance.getSoundType(this.level, pos, null);
        }

        return original.call(instance);
    }

    @WrapOperation(method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private static int kilt$tryUseForgeLightEmission(BlockState instance, Operation<Integer> original, @Local(argsOnly = true) BlockAndTintGetter level, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), Block.class, "getLightEmission", BlockState.class, BlockAndTintGetter.class, BlockPos.class)) {
            return instance.getLightEmission(level, pos);
        }

        return original.call(instance);
    }
}