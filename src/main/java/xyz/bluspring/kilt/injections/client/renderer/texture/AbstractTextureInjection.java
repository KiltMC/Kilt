package xyz.bluspring.kilt.injections.client.renderer.texture;

import io.github.fabricators_of_create.porting_lib.extensions.client.AbstractTextureExtension;
import xyz.bluspring.kilt.util.KiltHelper;

public interface AbstractTextureInjection extends AbstractTextureExtension {
    default void setBlurMipmap(boolean blur, boolean mipmap) {
        throw KiltHelper.createMixinException(AbstractTextureInjection.class, "setBlurMipmap");
    }

    default void restoreLastBlurMipmap() {
        throw KiltHelper.createMixinException(AbstractTextureInjection.class, "restoreLastBlurMipmap");
    }
}
