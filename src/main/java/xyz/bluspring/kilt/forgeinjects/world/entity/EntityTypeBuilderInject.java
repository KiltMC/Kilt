package xyz.bluspring.kilt.forgeinjects.world.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.entity.EntityTypeInjection;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

@Mixin(EntityType.Builder.class)
public class EntityTypeBuilderInject<T extends Entity> {
    private BiFunction<PlayMessages.SpawnEntity, Level, T> customClientFactory;
    private Predicate<EntityType<?>> velocityUpdateSupplier;
    private ToIntFunction<EntityType<?>> trackingRangeSupplier;
    private ToIntFunction<EntityType<?>> updateIntervalSupplier;

    // for all intents and purposes, mixin should add these methods in
    public EntityType.Builder<T> setUpdateInterval(int interval) {
        updateIntervalSupplier = t -> interval;
        return (EntityType.Builder<T>) (Object) this;
    }

    public EntityType.Builder<T> setTrackingRange(int range) {
        trackingRangeSupplier = t -> range;
        return (EntityType.Builder<T>) (Object) this;
    }

    public EntityType.Builder<T> setShouldReceiveVelocityUpdates(boolean value) {
        velocityUpdateSupplier = t -> value;
        return (EntityType.Builder<T>) (Object) this;
    }

    public EntityType.Builder<T> setCustomClientFactory(BiFunction<PlayMessages.SpawnEntity, Level, T> factory) {
        customClientFactory = factory;
        return (EntityType.Builder<T>) (Object) this;
    }

    @Inject(at = @At("RETURN"), method = "build")
    public void kilt$addForgeBuilderItems(String string, CallbackInfoReturnable<EntityType<T>> cir) {
        var entityType = (EntityTypeInjection<T>) cir.getReturnValue();
        entityType.setCustomClientFactory(customClientFactory);
        entityType.setTrackingRangeSupplier(trackingRangeSupplier);
        entityType.setUpdateIntervalSupplier(updateIntervalSupplier);
        entityType.setVelocityUpdateSupplier(velocityUpdateSupplier);
    }
}
