package xyz.bluspring.kilt.injects.client.particle;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL32C;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.particle.ParticleEngineInjection;

import java.util.*;
import java.util.function.Consumer;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineInject implements ParticleEngineInjection {
    @Shadow @Final @Mutable private Map<ParticleRenderType, Queue<Particle>> particles;
    @Shadow @Final private static List<ParticleRenderType> RENDER_ORDER;
    @Shadow protected ClientLevel level;
    @Shadow public abstract void crack(BlockPos pos, Direction side);

    @Shadow public abstract void render(LightTexture lightTexture, Camera camera, float f);

    @Unique private @Nullable Frustum kilt$clippingHelper;
    // Used by some Forge mods, so we need to patch it, but unfortunately also means we're storing this data twice.
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Unique private final Map<ResourceLocation, ParticleProvider<?>> kilt$providers = new HashMap<>();

    @WrapOperation(method = {"register(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/client/particle/ParticleProvider;)V", "register(Lnet/minecraft/core/particles/ParticleType;Lnet/minecraft/client/particle/ParticleEngine$SpriteParticleRegistration;)V"}, at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;put(ILjava/lang/Object;)Ljava/lang/Object;", remap = false))
    private <T extends ParticleOptions> Object kilt$registerToForgeProviders(Int2ObjectMap<?> instance, int i, Object o, Operation<Object> original, @Local(argsOnly = true) ParticleType<T> particleType) {
        this.kilt$providers.put(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType), (ParticleProvider<?>) o);
        return original.call(instance, i, o);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$handleParticleTypeComparator(ClientLevel level, TextureManager textureManager, CallbackInfo ci) {
        var oldMap = this.particles;
        this.particles = Maps.newTreeMap(ClientHooks.makeParticleRenderTypeComparator(RENDER_ORDER));

        this.particles.putAll(oldMap);
    }

    @Override
    public void kilt$setClippingHelper(Frustum frustum) {
        this.kilt$clippingHelper = frustum;
    }

    @Override
    public void render(LightTexture lightTexture, Camera camera, float tickDelta, @Nullable Frustum clippingHelper) {
        this.kilt$clippingHelper = clippingHelper;
        this.render(lightTexture, camera, tickDelta);
        this.kilt$clippingHelper = null;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableDepthTest()V", shift = At.Shift.AFTER, ordinal = 0, remap = false))
    private void kilt$initActiveTexture(LightTexture lightTexture, Camera camera, float partialTick, CallbackInfo ci) {
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

//    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
//    private <V> V kilt$removeIfClippingHelper(V original) {
//        if (kilt$clippingHelper != null) {
//            var list = (Iterable<Particle>) original;
//            var clippingHelper = kilt$clippingHelper;
//            return (V) Streams.stream(list).filter(p -> !(p.shouldCull() && !clippingHelper.isVisible(p.getBoundingBox()))).toList();
//        }
//
//        return original;
//    }

    @ModifyExpressionValue(method = "destroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;shouldSpawnTerrainParticles()Z"))
    private boolean kilt$callDestroyEffects(boolean original, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state) {
        return original || !IClientBlockExtensions.of(state).addDestroyEffects(state, this.level, pos, (ParticleEngine) (Object) this);
    }

    @ModifyExpressionValue(method = {"method_34020", "crack"}, at = @At(value = "NEW", target = "(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/client/particle/TerrainParticle;"))
    private TerrainParticle kilt$handleUpdateSprite(TerrainParticle original, @Local BlockState state, @Local(argsOnly = true) BlockPos pos) {
        return original.updateSprite(state, pos);
    }

    @Override
    public void iterateParticles(Consumer<Particle> consumer) {
        for (ParticleRenderType particleRenderType : this.particles.keySet()) {
            if (particleRenderType == ParticleRenderType.NO_RENDER)
                continue;

            Iterable<Particle> particles = this.particles.get(particleRenderType);
            if (particles != null) {
                particles.forEach(consumer);
            }
        }
    }

    @Override
    public void addBlockHitEffects(BlockPos pos, BlockHitResult target) {
        kilt$addBlockHitEffects(pos, target, target.getDirection(), args -> {
            ((ParticleEngine) args[0]).crack((BlockPos) args[1], (Direction) args[2]);
            return null;
        });
    }

    @Override
    public void kilt$addBlockHitEffects(BlockPos pos, BlockHitResult target, Direction direction, Operation<Void> original) {
        var state = this.level.getBlockState(pos);

        if (!IClientBlockExtensions.of(state).addHitEffects(state, this.level, target, (ParticleEngine) (Object) this))
            original.call(this, pos, direction);
    }

    @Override
    public Map<ResourceLocation, ParticleProvider<?>> kilt$getProviders() {
        return this.kilt$providers;
    }
}
