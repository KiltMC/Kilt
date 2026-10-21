package xyz.bluspring.kilt.injections.blaze3d.textures;

import xyz.bluspring.kilt.util.KiltHelper;

public interface TextureFormatInjection {
    default boolean hasStencilAspect() {
        throw KiltHelper.createMixinException(TextureFormatInjection.class, "hasStencilAspect");
    }
}
