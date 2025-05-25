package xyz.bluspring.kilt.forgeinjects.client.particle;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL32C;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.particle.ParticleEngineInjection;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineInject implements ParticleEngineInjection {
    @Shadow public abstract void render(PoseStack poseStack, MultiBufferSource.BufferSource buffer, LightTexture lightTexture, Camera activeRenderInfo, float partialTicks);
    @Shadow @Final @Mutable private Map<ParticleRenderType, Queue<Particle>> particles;
    @Shadow @Final private static List<ParticleRenderType> RENDER_ORDER;

    @Shadow protected ClientLevel level;

    @Shadow public abstract void crack(BlockPos pos, Direction side);

    @Unique private @Nullable Frustum kilt$clippingHelper;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$handleParticleTypeComparator(ClientLevel level, TextureManager textureManager, CallbackInfo ci) {
        var oldMap = this.particles;
        this.particles = Maps.newTreeMap(ForgeHooksClient.makeParticleRenderTypeComparator(RENDER_ORDER));

        this.particles.putAll(oldMap);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LightTexture lightTexture, Camera camera, float tickDelta, @Nullable Frustum clippingHelper) {
        this.kilt$clippingHelper = clippingHelper;
        this.render(poseStack, bufferSource, lightTexture, camera, tickDelta);
        this.kilt$clippingHelper = null;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableDepthTest()V", shift = At.Shift.AFTER, ordinal = 0))
    private void kilt$initActiveTexture(PoseStack poseStack, MultiBufferSource.BufferSource buffer, LightTexture lightTexture, Camera activeRenderInfo, float partialTicks, CallbackInfo ci) {
        RenderSystem.activeTexture(GL32C.GL_TEXTURE2);
        RenderSystem.activeTexture(GL32C.GL_TEXTURE0);
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ParticleEngine;RENDER_ORDER:Ljava/util/List;"))
    private List<ParticleRenderType> kilt$mergeCustomParticles(List<ParticleRenderType> original) {
        var set = new LinkedHashSet<ParticleRenderType>();
        set.addAll(RENDER_ORDER);
        set.addAll(this.particles.keySet());

        return set.stream().toList();
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private <V> V kilt$removeIfClippingHelper(V original) {
        if (kilt$clippingHelper != null) {
            var list = (Iterable<Particle>) original;
            var clippingHelper = kilt$clippingHelper;
            return (V) Streams.stream(list).filter(p -> !(p.shouldCull() && !clippingHelper.isVisible(p.getBoundingBox()))).toList();
        }

        return original;
    }

    @ModifyExpressionValue(method = "destroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;shouldSpawnParticlesOnBreak()Z"))
    private boolean kilt$callDestroyEffects(boolean original, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state) {
        return original || !IClientBlockExtensions.of(state).addDestroyEffects(state, this.level, pos, (ParticleEngine) (Object) this);
    }

    @ModifyExpressionValue(method = {"method_34020", "crack"}, at = @At(value = "NEW", target = "(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/client/particle/TerrainParticle;"))
    private TerrainParticle kilt$handleUpdateSprite(TerrainParticle original, @Local BlockState state, @Local(argsOnly = true) BlockPos pos) {
        return original.updateSprite(state, pos);
    }

    @Override
    public void addBlockHitEffects(BlockPos pos, BlockHitResult target) {
        var state = this.level.getBlockState(pos);

        if (!IClientBlockExtensions.of(state).addHitEffects(state, this.level, target, (ParticleEngine) (Object) this))
            this.crack(pos, target.getDirection());
    }
}
