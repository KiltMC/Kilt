// TRACKED HASH: 10700ce9768d4e28899f348e8a85eb5186b5ef0e
package xyz.bluspring.kilt.forgeinjects.world.level.biome;

import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.biome.BiomeGenerationSettingsInjection;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(BiomeGenerationSettings.class)
public class BiomeGenerationSettingsInject implements BiomeGenerationSettingsInjection {
    @Unique private Set<GenerationStep.Carving> carversView;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$addCarveStages(Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>> carvers, List<HolderSet<PlacedFeature>> features, CallbackInfo ci) {
        this.carversView = Collections.unmodifiableSet(carvers.keySet());
    }

    @Override
    public Set<GenerationStep.Carving> getCarvingStages() {
        return carversView;
    }
}