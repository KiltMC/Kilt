package xyz.bluspring.kilt.injections.world.item;

public interface ItemStackInjection {
    default boolean isComponentsPatchEmpty() {
        throw new IllegalStateException();
    }
}
