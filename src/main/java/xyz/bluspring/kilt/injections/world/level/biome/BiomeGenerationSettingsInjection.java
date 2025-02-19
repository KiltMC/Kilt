package xyz.bluspring.kilt.injections.world.level.biome;

import net.minecraft.world.level.levelgen.GenerationStep;

import java.util.Set;

public interface BiomeGenerationSettingsInjection {
    default Set<GenerationStep.Carving> getCarvingStages() {
        throw new IllegalStateException();
    }
}
