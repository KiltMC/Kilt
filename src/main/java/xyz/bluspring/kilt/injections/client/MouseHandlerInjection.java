package xyz.bluspring.kilt.injections.client;

public interface MouseHandlerInjection {
    default double getXVelocity() {
        throw new IllegalStateException();
    }

    default double getYVelocity() {
        throw new IllegalStateException();
    }
}
