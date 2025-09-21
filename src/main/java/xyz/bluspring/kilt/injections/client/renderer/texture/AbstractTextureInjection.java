package xyz.bluspring.kilt.injections.client.renderer.texture;

import io.github.fabricators_of_create.porting_lib.extensions.client.AbstractTextureExtension;

public interface AbstractTextureInjection extends AbstractTextureExtension {
    void setBlurMipmap(boolean blur, boolean mipmap);
    void restoreLastBlurMipmap();
}
