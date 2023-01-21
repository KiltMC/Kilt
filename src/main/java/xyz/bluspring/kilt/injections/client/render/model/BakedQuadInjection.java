package xyz.bluspring.kilt.injections.client.render.model;

public interface BakedQuadInjection {
    default boolean hasAmbientOcclusion() {
        return true;
    }
}
