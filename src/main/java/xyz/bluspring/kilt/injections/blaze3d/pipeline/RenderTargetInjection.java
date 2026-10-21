package xyz.bluspring.kilt.injections.blaze3d.pipeline;

import xyz.bluspring.kilt.util.KiltHelper;

public interface RenderTargetInjection {
    default boolean kilt$useStencil() {
        throw KiltHelper.createMixinException(RenderTargetInjection.class, "kilt$useStencil");
    }

    default void kilt$setStencil(boolean useStencil) {
        throw KiltHelper.createMixinException(RenderTargetInjection.class, "kilt$setStencil");
    }
}
