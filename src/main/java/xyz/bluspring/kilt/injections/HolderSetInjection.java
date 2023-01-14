package xyz.bluspring.kilt.injections;

public interface HolderSetInjection {
    default void addInvalidationListener(Runnable runnable) {
    }
}