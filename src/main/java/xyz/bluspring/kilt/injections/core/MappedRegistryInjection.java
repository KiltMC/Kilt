package xyz.bluspring.kilt.injections.core;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public interface MappedRegistryInjection {
    Set<ResourceLocation> knownRegistries = new LinkedHashSet<>();

    static Set<ResourceLocation> getKnownRegistries() {
        return Collections.unmodifiableSet(knownRegistries);
    }

    default void markKnown() {
        throw new IllegalStateException();
    }

    default void unfreeze() {
        throw new IllegalStateException();
    }
}
