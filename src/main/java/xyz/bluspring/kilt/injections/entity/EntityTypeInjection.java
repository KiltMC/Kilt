package xyz.bluspring.kilt.injections.entity;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

public interface EntityTypeInjection<T extends Entity> {
    default T customClientSpawn(PlayMessages.SpawnEntity packet, Level world) {
        throw new IllegalStateException();
    }

    default void setVelocityUpdateSupplier(Predicate<EntityType<?>> supplier) {
        throw new IllegalStateException();
    }

    default void setTrackingRangeSupplier(ToIntFunction<EntityType<?>> supplier) {
        throw new IllegalStateException();
    }

    default void setUpdateIntervalSupplier(ToIntFunction<EntityType<?>> supplier) {
        throw new IllegalStateException();
    }

    default void setCustomClientFactory(BiFunction<PlayMessages.SpawnEntity, Level, T> factory) {
        throw new IllegalStateException();
    }

    default Stream<TagKey<EntityType<?>>> getTags() {
        throw new IllegalStateException();
    }
}
