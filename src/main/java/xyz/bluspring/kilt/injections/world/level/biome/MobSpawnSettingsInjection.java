package xyz.bluspring.kilt.injections.world.level.biome;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.Set;

public interface MobSpawnSettingsInjection {
    default Set<MobCategory> getSpawnerTypes() {
        throw new IllegalStateException();
    }

    default Set<EntityType<?>> getEntityTypes() {
        throw new IllegalStateException();
    }
}
