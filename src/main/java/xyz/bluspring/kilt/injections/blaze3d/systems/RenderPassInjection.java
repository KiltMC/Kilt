package xyz.bluspring.kilt.injections.blaze3d.systems;

import xyz.bluspring.kilt.util.KiltHelper;

public interface RenderPassInjection {
    default void setViewport(int x, int y, int width, int height) {
        throw KiltHelper.createMixinException(RenderPassInjection.class, "setViewport");
    }
}
