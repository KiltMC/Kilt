package xyz.bluspring.kilt.injections.client.render;

public interface RenderPropertiesInjection {
    // Forge's comments say to call RenderProperties.get, but there's a problem.
    // wHERE THE FUCK IS IT
    // In the meantime, let's just do this.
    default Object getRenderPropertiesInternal() {
        throw new IllegalStateException();
    }
}
