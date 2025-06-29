package xyz.bluspring.kilt.injections.core;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;

public interface HolderLookupInjection {
    public interface RegistryLookupInjection<T> {
        @Nullable
        default <A> A getData(DataMapType<T, A> attachment, ResourceKey<T> key) {
            return null;
        }
    }
}
