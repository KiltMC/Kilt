package xyz.bluspring.kilt.injections.world.biome;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public interface BiomeInjection {
    default ModifiableBiomeInfo modifiableBiomeInfo() {
        throw new IllegalStateException();
    }

    default Biome.ClimateSettings getModifiedClimateSettings() {
        throw new IllegalStateException();
    }

    default BiomeSpecialEffects getModifiedSpecialEffects() {
        throw new IllegalStateException();
    }
}
