package xyz.bluspring.kilt.injections.blaze3d.pipeline;

import xyz.bluspring.kilt.util.KiltHelper;

public interface RenderTargetInjection {
    default void enableStencil() {
        throw KiltHelper.createMixinException(RenderTargetInjection.class, "enableStencil");
    }

    default void disableStencil() {
        throw KiltHelper.createMixinException(RenderTargetInjection.class, "disableStencil");
    }

    default boolean isStencilEnabled() {
        throw KiltHelper.createMixinException(RenderTargetInjection.class, "isStencilEnabled");
    }
}
