package xyz.bluspring.kilt.injections.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelReader;
import xyz.bluspring.kilt.remaps.world.level.NaturalSpawnerRemap;

public interface NaturalSpawnerInjection {
    static boolean canSpawnAtBody(SpawnPlacements.Type type, LevelReader levelReader, BlockPos blockPos, EntityType<?> entityType) {
        // redirecting to Kotlin for sanity reasons
        return NaturalSpawnerRemap.canSpawnAtBody(type, levelReader, blockPos, entityType);
    }
}
