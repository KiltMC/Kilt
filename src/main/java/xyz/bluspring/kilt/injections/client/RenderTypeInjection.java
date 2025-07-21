package xyz.bluspring.kilt.injections.client;

import net.minecraft.client.renderer.RenderType;

import java.util.concurrent.atomic.AtomicInteger;

public interface RenderTypeInjection {
    AtomicInteger kilt$loadedChunkLayers = new AtomicInteger(0);

    default int getChunkLayerId() {
        throw new RuntimeException("mixin.");
    }

    default void setChunkLayerId(int id) {
        throw new RuntimeException("mixin.");
    }

    static void kilt$initLoadedChunkLayers() {
        var layers = RenderType.chunkBufferLayers();
        if (layers.size() == kilt$loadedChunkLayers.get())
            return;

        var i = 0;
        for (var layer : layers)
            layer.setChunkLayerId(i++);

        kilt$loadedChunkLayers.set(layers.size());
    }
}
