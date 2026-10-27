package xyz.bluspring.kilt.injections.client.gui.render.pip;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

public interface PictureInPictureRendererInjection<T extends PictureInPictureRenderState> {
    default boolean canBeReusedFor(T state, int textureWidth, int textureHeight) {
        throw KiltHelper.createMixinException(PictureInPictureRendererInjection.class, "canBeReusedFor");
    }
}
