package xyz.bluspring.kilt.injections.core;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.resources.ResourceKey;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

import java.util.List;

@FabricInjectedInterface(RegistrySetBuilder.class)
public interface RegistrySetBuilderInjection {
    List<? extends ResourceKey<? extends Registry<?>>> getEntryKeys();
}
