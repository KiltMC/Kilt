package xyz.bluspring.kilt.injections.core;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(HolderLookup.RegistryLookup.class)
public interface HolderLookupInjection {
    interface RegistryLookupInjection<T> {
        @Nullable
        default <A> A getData(DataMapType<T, A> attachment, ResourceKey<T> key) {
            return null;
        }
    }
}
