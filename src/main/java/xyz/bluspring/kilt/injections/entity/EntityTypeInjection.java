package xyz.bluspring.kilt.injections.entity;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

public interface EntityTypeInjection<T extends Entity> {
    default void kilt$setVelocityUpdateSupplier(Predicate<EntityType<?>> supplier) {
        throw new IllegalStateException();
    }
    default void kilt$setTrackingRangeSupplier(ToIntFunction<EntityType<?>> supplier) {
        throw new IllegalStateException();
    }
    default void kilt$setUpdateIntervalSupplier(ToIntFunction<EntityType<?>> supplier) {
        throw new IllegalStateException();
    }

    default Stream<TagKey<EntityType<?>>> getTags() {
        throw new IllegalStateException();
    }
}
