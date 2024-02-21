package xyz.bluspring.kilt.injections.data.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import xyz.bluspring.kilt.injections.core.RegistrySetBuilderInjection;
import xyz.bluspring.kilt.mixin.data.registries.VanillaRegistriesAccessor;

import java.util.List;

public interface VanillaRegistriesInjection {
    List<? extends ResourceKey<? extends Registry<?>>> DATAPACK_REGISTRY_KEYS = ((RegistrySetBuilderInjection) VanillaRegistriesAccessor.getBuilder()).getEntryKeys();
}
