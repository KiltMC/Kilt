package xyz.bluspring.kilt.injections.world.level.block.entity;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.block.entity.FuelValues;

public interface FuelValuesInjection {
    ThreadLocal<FuelValues.Builder> kilt$activeBuilder = new ThreadLocal<>();

    static FuelValues vanillaBurnTimes(FuelValues.Builder builder, int baseUnit) {
        try {
            kilt$activeBuilder.set(builder);
            return FuelValues.vanillaBurnTimes(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), FeatureFlagSet.of(), baseUnit);
        } finally {
            kilt$activeBuilder.remove();
        }
    }
}
