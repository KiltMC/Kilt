package xyz.bluspring.kilt.injections.world.level.biome;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Set;

public interface MobSpawnSettingsInjection {
    default Set<MobCategory> getSpawnerTypes() {
        throw KiltHelper.createMixinException(MobSpawnSettingsInjection.class, "getSpawnerTypes");
    }

    default Set<EntityType<?>> getEntityTypes() {
        throw KiltHelper.createMixinException(MobSpawnSettingsInjection.class, "getEntityTypes");
    }
}
