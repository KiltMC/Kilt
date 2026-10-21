package xyz.bluspring.kilt.injections.client.renderer.state.level;

import xyz.bluspring.kilt.util.KiltHelper;

public interface CameraEntityRenderStateInjection {
    default boolean kilt$preventFlinch() {
        throw KiltHelper.createMixinException(CameraEntityRenderStateInjection.class, "kilt$preventFlinch");
    }

    default void kilt$preventFlinch(boolean value) {
        throw KiltHelper.createMixinException(CameraEntityRenderStateInjection.class, "kilt$preventFlinch");
    }
}
