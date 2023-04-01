package xyz.bluspring.kilt.injections.core;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public interface MappedRegistryInjection {
    Set<ResourceLocation> knownRegistries = new HashSet<>();

    static Set<ResourceLocation> getKnownRegistries() {
        return knownRegistries;
    }

    default void markKnown() {
        throw new IllegalStateException();
    }

    default void unfreeze() {
        throw new IllegalStateException();
    }
}
