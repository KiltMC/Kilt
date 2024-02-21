package xyz.bluspring.kilt.injections.core;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.List;

public interface RegistrySetBuilderInjection {
    List<? extends ResourceKey<? extends Registry<?>>> getEntryKeys();
}
