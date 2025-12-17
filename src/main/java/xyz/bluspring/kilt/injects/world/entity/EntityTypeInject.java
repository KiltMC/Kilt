// TRACKED HASH: 285b05db3877c1ccdd76c7704e1de0c69adff005
package xyz.bluspring.kilt.injects.world.entity;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.entity.EntityTypeInjection;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

@Mixin(EntityType.class)
public abstract class EntityTypeInject<T extends Entity> implements EntityTypeInjection<T> {
    @SuppressWarnings("MixinAnnotationTarget") // this is supposed to exist, but I guess mixin doesn't think so?
    @Shadow @Nullable public abstract T create(Level level);
    @Shadow @Final private Holder.Reference<EntityType<?>> builtInRegistryHolder;
    @Shadow @Final private int clientTrackingRange;
    @Shadow @Final private int updateInterval;

    @Unique private Predicate<EntityType<?>> trackDeltasSupplier;
    @Unique private ToIntFunction<EntityType<?>> trackingRangeSupplier;
    @Unique private ToIntFunction<EntityType<?>> updateIntervalSupplier;

    @Override
    public void kilt$setVelocityUpdateSupplier(Predicate<EntityType<?>> supplier) {
        trackDeltasSupplier = supplier;
    }

    @Override
    public void kilt$setTrackingRangeSupplier(ToIntFunction<EntityType<?>> supplier) {
        trackingRangeSupplier = supplier;
    }

    @Override
    public void kilt$setUpdateIntervalSupplier(ToIntFunction<EntityType<?>> supplier) {
        updateIntervalSupplier = supplier;
    }

    @Override
    public Stream<TagKey<EntityType<?>>> getTags() {
        return this.builtInRegistryHolder.tags();
    }

    @Inject(at = @At("HEAD"), method = "clientTrackingRange", cancellable = true)
    public void kilt$useForgeTrackingRange(CallbackInfoReturnable<Integer> cir) {
        if (trackingRangeSupplier != null)
            cir.setReturnValue(trackingRangeSupplier.applyAsInt((EntityType<?>) (Object) this));
    }

    @Inject(at = @At("HEAD"), method = "updateInterval", cancellable = true)
    public void kilt$useForgeUpdateInterval(CallbackInfoReturnable<Integer> cir) {
        if (updateIntervalSupplier != null)
            cir.setReturnValue(updateIntervalSupplier.applyAsInt((EntityType<?>) (Object) this));
    }

    @Inject(at = @At("HEAD"), method = "trackDeltas", cancellable = true)
    public void kilt$useForgeVelocityUpdate(CallbackInfoReturnable<Boolean> cir) {
        if (trackDeltasSupplier != null)
            cir.setReturnValue(trackDeltasSupplier.test((EntityType<?>) (Object) this));
    }

    // TODO: any better ideas?
    private int defaultTrackingRangeSupplier() {
        return this.clientTrackingRange;
    }

    private int defaultUpdateIntervalSupplier() {
        return this.updateInterval;
    }

    private boolean defaultVelocitySupplier() {
        var self = (EntityType<?>) (Object) this;
        return self != EntityType.PLAYER && self != EntityType.LLAMA_SPIT && self != EntityType.WITHER && self != EntityType.BAT && self != EntityType.ITEM_FRAME && self != EntityType.GLOW_ITEM_FRAME && self != EntityType.LEASH_KNOT && self != EntityType.PAINTING && self != EntityType.END_CRYSTAL && self != EntityType.EVOKER_FANGS;
    }

    @Mixin(EntityType.Builder.class)
    public static class BuilderInject<T extends Entity> {
        private Predicate<EntityType<?>> velocityUpdateSupplier;
        private ToIntFunction<EntityType<?>> trackingRangeSupplier;
        private ToIntFunction<EntityType<?>> updateIntervalSupplier;

        // for all intents and purposes, mixin should add these methods in
        @Unique
        public EntityType.Builder<T> setUpdateInterval(int interval) {
            updateIntervalSupplier = t -> interval;
            return (EntityType.Builder<T>) (Object) this;
        }

        @Unique
        public EntityType.Builder<T> setTrackingRange(int range) {
            trackingRangeSupplier = t -> range;
            return (EntityType.Builder<T>) (Object) this;
        }

        @Unique
        public EntityType.Builder<T> setShouldReceiveVelocityUpdates(boolean value) {
            velocityUpdateSupplier = t -> value;
            return (EntityType.Builder<T>) (Object) this;
        }

        @Inject(at = @At("RETURN"), method = "build")
        public void kilt$addForgeBuilderItems(String string, CallbackInfoReturnable<EntityType<T>> cir) {
            var entityType = (EntityTypeInjection<T>) cir.getReturnValue();
            entityType.kilt$setTrackingRangeSupplier(trackingRangeSupplier);
            entityType.kilt$setUpdateIntervalSupplier(updateIntervalSupplier);
            entityType.kilt$setVelocityUpdateSupplier(velocityUpdateSupplier);
        }
    }

}