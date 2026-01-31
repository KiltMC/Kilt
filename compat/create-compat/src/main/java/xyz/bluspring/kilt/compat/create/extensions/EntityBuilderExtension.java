package xyz.bluspring.kilt.compat.create.extensions;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public interface EntityBuilderExtension<T> {

//    void kilt$setCustomClientFactory(BiFunction<PlayMessages.SpawnEntity, Level, T> customClientFactory);

    void kilt$setVelocityUpdateSupplier(Predicate<EntityType<?>> velocityUpdateSupplier);

    void kilt$setTrackingRangeSupplier(ToIntFunction<EntityType<?>> trackingRangeSupplier);

    void kilt$setUpdateIntervalSupplier(ToIntFunction<EntityType<?>> updateIntervalSupplier);

}
