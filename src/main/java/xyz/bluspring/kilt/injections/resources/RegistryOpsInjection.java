package xyz.bluspring.kilt.injections.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import xyz.bluspring.kilt.mixin.RegistryOpsAccessor;

public interface RegistryOpsInjection {
    static <E> MapCodec<HolderLookup.RegistryLookup<E>> retrieveRegistryLookup(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return ExtraCodecs.retrieveContext(ops -> {
            if (!(ops instanceof RegistryOps<?> registryOps))
                return DataResult.error(() -> "Not a registry ops");

            return ((RegistryOpsAccessor) registryOps).getLookupProvider().lookup(resourceKey).map(registryInfo -> {
                if (!(registryInfo.owner() instanceof HolderLookup.RegistryLookup<E> registryLookup))
                    return DataResult.<HolderLookup.RegistryLookup<E>>error(() -> "Found holder getter but was not a registry lookup for " + resourceKey);

                return DataResult.success(registryLookup, registryInfo.elementsLifecycle());
            }).orElseGet(() -> DataResult.error(() -> "Unknown registry: " + resourceKey));
        });
    }
}
