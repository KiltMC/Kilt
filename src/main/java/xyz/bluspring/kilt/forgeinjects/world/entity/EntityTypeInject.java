// TRACKED HASH: 285b05db3877c1ccdd76c7704e1de0c69adff005
package xyz.bluspring.kilt.forgeinjects.world.entity;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.entity.EntityTypeInjection;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

@Mixin(EntityType.class)
public abstract class EntityTypeInject<T extends Entity> implements EntityTypeInjection<T> {
    @SuppressWarnings("MixinAnnotationTarget") // this is supposed to exist, but I guess mixin doesn't think so?
    @Shadow @Nullable public abstract T create(Level level);

    @Shadow @Final private Holder.Reference<EntityType<?>> builtInRegistryHolder;
    private BiFunction<PlayMessages.SpawnEntity, Level, T> customClientFactory;
    private Predicate<EntityType<?>> velocityUpdateSupplier;
    private ToIntFunction<EntityType<?>> trackingRangeSupplier;
    private ToIntFunction<EntityType<?>> updateIntervalSupplier;

    @Override
    public T customClientSpawn(PlayMessages.SpawnEntity packet, Level world) {
        if (customClientFactory == null)
            return this.create(world);

        return customClientFactory.apply(packet, world);
    }

    @Override
    public void setVelocityUpdateSupplier(Predicate<EntityType<?>> supplier) {
        velocityUpdateSupplier = supplier;
    }

    @Override
    public void setTrackingRangeSupplier(ToIntFunction<EntityType<?>> supplier) {
        trackingRangeSupplier = supplier;
    }

    @Override
    public void setUpdateIntervalSupplier(ToIntFunction<EntityType<?>> supplier) {
        updateIntervalSupplier = supplier;
    }

    @Override
    public void setCustomClientFactory(BiFunction<PlayMessages.SpawnEntity, Level, T> factory) {
        this.customClientFactory = factory;
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
        if (velocityUpdateSupplier != null)
            cir.setReturnValue(velocityUpdateSupplier.test((EntityType<?>) (Object) this));
    }
}