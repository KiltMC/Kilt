package xyz.bluspring.kilt.injections.client.particle;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;

public interface ParticleEngineInjection {
    default void kilt$setClippingHelper(Frustum frustum) {}
    default void render(LightTexture lightTexture, Camera camera, float tickDelta, @Nullable Frustum clippingHelper, Predicate<ParticleRenderType> renderTypePredicate) {}
    default void addBlockHitEffects(BlockPos pos, BlockHitResult target) {}
    default void kilt$addBlockHitEffects(BlockPos pos, BlockHitResult target, Direction direction, Operation<Void> original) {}

    default void iterateParticles(Consumer<Particle> consumer) {
        throw KiltHelper.createMixinException(ParticleEngineInjection.class, "iterateParticles");
    }

    default Map<ResourceLocation, ParticleProvider<?>> kilt$getProviders() {
        throw KiltHelper.createMixinException(ParticleEngineInjection.class, "kilt$getProviders");
    }
}
