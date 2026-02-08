package xyz.bluspring.kilt.injections.core;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

@FabricInjectedInterface(MappedRegistry.class)
public interface MappedRegistryInjection<T> {
    default void unfreeze() {
        throw new IllegalStateException();
    }

    default Holder.Reference<T> register(int id, ResourceKey<T> key, T value, RegistrationInfo info) {
        throw new IllegalStateException();
    }

    default void registerIdMapping(ResourceKey<T> key, int id) {
        throw KiltHelper.createMixinException(MappedRegistryInjection.class, "registerIdMapping");
    }

    default void clear(boolean full) {
        throw KiltHelper.createMixinException(MappedRegistryInjection.class, "clear");
    }

    // Kilt: mainly used to call the super, honestly.
    default void kilt$clear(boolean full) {}
}
