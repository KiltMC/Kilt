package xyz.bluspring.kilt.injections.data.worldgen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

import java.util.Optional;

@FabricInjectedInterface(BootstrapContext.class)
public interface BootstrapContextInjection {
    default <S> Optional<HolderLookup.RegistryLookup<S>> registryLookup(ResourceKey<? extends Registry<? extends S>> registry) {
        return java.util.Optional.empty();
    }
}
