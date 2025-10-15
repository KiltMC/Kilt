// TRACKED HASH: af62a88eefa98a16ebc1fc0d8df684f0103f4686
package xyz.bluspring.kilt.forgeinjects.world.level.biome;

import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.biome.MobSpawnSettingsInjection;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Mixin(MobSpawnSettings.class)
public class MobSpawnSettingsInject implements MobSpawnSettingsInjection {
    @Shadow @Final private Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> spawners;
    @Shadow @Final private Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts;

    @Override
    public Set<MobCategory> getSpawnerTypes() {
        return Collections.unmodifiableSet(this.spawners.keySet());
    }

    @Override
    public Set<EntityType<?>> getEntityTypes() {
        return Collections.unmodifiableSet(this.mobSpawnCosts.keySet());
    }
}