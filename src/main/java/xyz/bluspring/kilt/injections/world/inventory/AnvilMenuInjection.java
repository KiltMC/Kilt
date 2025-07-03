package xyz.bluspring.kilt.injections.world.inventory;

public interface AnvilMenuInjection {
    default void setMaximumCost(long value) {
        throw new IllegalStateException();
    }
}
