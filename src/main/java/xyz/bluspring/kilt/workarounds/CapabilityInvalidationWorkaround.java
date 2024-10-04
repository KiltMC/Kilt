package xyz.bluspring.kilt.workarounds;

public interface CapabilityInvalidationWorkaround {
    default void kilt$invalidateCaps() {
        throw new IllegalStateException("this shouldn't happen");
    }
}
