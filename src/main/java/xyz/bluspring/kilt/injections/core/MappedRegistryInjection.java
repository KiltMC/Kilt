package xyz.bluspring.kilt.injections.core;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public interface MappedRegistryInjection<T> {
    default void unfreeze() {
        throw new IllegalStateException();
    }

    default Holder.Reference<T> register(int id, ResourceKey<T> key, T value, RegistrationInfo info) {
        throw new IllegalStateException();
    }

    // Kilt: mainly used to call the super, honestly.
    default void kilt$clear(boolean full) {}
}
