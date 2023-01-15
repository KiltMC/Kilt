package xyz.bluspring.kilt.injections.client;

public interface RenderTypeInjection {
    default int getChunkLayerId() {
        throw new RuntimeException("mixin.");
    }

    default void setChunkLayerId(int id) {
        throw new RuntimeException("mixin.");
    }
}
