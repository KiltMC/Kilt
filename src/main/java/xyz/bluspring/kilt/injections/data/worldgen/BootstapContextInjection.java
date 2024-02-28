package xyz.bluspring.kilt.injections.data.worldgen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

public interface BootstapContextInjection {
    default <S> Optional<HolderLookup.RegistryLookup<S>> registryLookup(ResourceKey<? extends Registry<? extends S>> registry) {
        return java.util.Optional.empty();
    }
}
