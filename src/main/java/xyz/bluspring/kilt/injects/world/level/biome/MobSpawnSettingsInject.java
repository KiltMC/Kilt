// TRACKED HASH: af62a88eefa98a16ebc1fc0d8df684f0103f4686
package xyz.bluspring.kilt.injects.world.level.biome;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.biome.MobSpawnSettingsInjection;

import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

@Mixin(MobSpawnSettings.class)
public abstract class MobSpawnSettingsInject implements MobSpawnSettingsInjection {
    @Shadow @Final private Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts;

    @Shadow
    @Final
    private Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawners;
    @Unique private Set<MobCategory> typesView;
    @Unique private Set<EntityType<?>> costView;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initViews(float creatureGenerationProbability, Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawners, Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts, CallbackInfo ci) {
        this.typesView = Collections.unmodifiableSet(this.spawners.keySet());
        this.costView = Collections.unmodifiableSet(this.mobSpawnCosts.keySet());
    }

    @Unique
    private void kilt$tryRebuildViews() {
        if (this.typesView.hashCode() != this.spawners.hashCode()) {
            this.typesView = Collections.unmodifiableSet(this.spawners.keySet());
        }

        if (this.costView.hashCode() != this.mobSpawnCosts.hashCode()) {
            this.costView = Collections.unmodifiableSet(this.mobSpawnCosts.keySet());
        }
    }

    @Override
    public Set<MobCategory> getSpawnerTypes() {
        this.kilt$tryRebuildViews();
        return this.typesView;
    }

    @Override
    public Set<EntityType<?>> getEntityTypes() {
        this.kilt$tryRebuildViews();
        return this.costView;
    }
}
