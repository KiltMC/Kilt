package xyz.bluspring.kilt.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

@Mixin(MobSpawnSettings.class)
public interface MobSpawnSettingsAccessor {
    @Accessor
    Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> getSpawners();

    @Accessor
    Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> getMobSpawnCosts();
}
