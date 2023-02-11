package xyz.bluspring.kilt.injections.world.level.block;

public interface PoweredRailBlockInjection {
    default void registerDefaultState() {
        throw new IllegalStateException();
    }

    default boolean isActivatorRail() {
        throw new IllegalStateException();
    }

    default void kilt$setActivator(boolean value) {
        throw new IllegalStateException();
    }
}
