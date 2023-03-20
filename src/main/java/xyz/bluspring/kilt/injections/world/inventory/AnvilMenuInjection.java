package xyz.bluspring.kilt.injections.world.inventory;

public interface AnvilMenuInjection {
    default void setMaximumCost(int value) {
        throw new IllegalStateException();
    }
}
