package xyz.bluspring.kilt.injections.client.render;

import java.util.function.Consumer;

public interface RenderPropertiesInjection<T> {
    // Forge's comments say to call RenderProperties.get, but there's a problem.
    // wHERE THE FUCK IS IT
    // In the meantime, let's just do this.
    default Object getRenderPropertiesInternal() {
        throw new IllegalStateException();
    }

    // This is a funny little workaround to make this work for everything
    default void initializeClient(Consumer<T> consumer) {}
}
