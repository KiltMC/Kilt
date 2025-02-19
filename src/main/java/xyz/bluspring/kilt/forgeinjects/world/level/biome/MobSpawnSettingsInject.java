package xyz.bluspring.kilt.forgeinjects.world.level.biome;

import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.biome.MobSpawnSettingsInjection;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Mixin(MobSpawnSettings.class)
public abstract class MobSpawnSettingsInject implements MobSpawnSettingsInjection {
    @Unique private Set<MobCategory> typesView;
    @Unique private Set<EntityType<?>> costView;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initMobSpawnViews(float creatureGenerationProbability, Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> spawners, Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts, CallbackInfo ci) {
        this.typesView = Collections.unmodifiableSet(spawners.keySet());
        this.costView = Collections.unmodifiableSet(mobSpawnCosts.keySet());
    }

    @Override
    public Set<MobCategory> getSpawnerTypes() {
        return typesView;
    }

    @Override
    public Set<EntityType<?>> getEntityTypes() {
        return costView;
    }
}
