package xyz.bluspring.kilt.injections;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.function.Supplier;

// Due to a mixin issue where you can't extend abstract classes inside a mixin, this is a
// little helper to help redirect method calls to CapabilityProvider, while also
// allowing the ability to override the methods. Very handy.
public interface CapabilityProviderInjection {
    default void gatherCapabilities() {
        throw new IllegalStateException();
    }

    default void gatherCapabilities(@Nullable ICapabilityProvider parent) {
        throw new IllegalStateException();
    }

    default void gatherCapabilities(@Nullable Supplier<ICapabilityProvider> parent) {
        throw new IllegalStateException();
    }

    @Nullable
    default CapabilityDispatcher getCapabilities() {
        throw new IllegalStateException();
    }

    @Nullable
    default CompoundTag serializeCaps() {
        throw new IllegalStateException();
    }

    default void deserializeCaps(CompoundTag tag) {
        throw new IllegalStateException();
    }
}
