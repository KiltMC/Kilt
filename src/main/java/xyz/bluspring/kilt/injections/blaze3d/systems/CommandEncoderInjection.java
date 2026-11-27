package xyz.bluspring.kilt.injections.blaze3d.systems;

import com.mojang.blaze3d.textures.GpuTexture;
import xyz.bluspring.kilt.util.KiltHelper;

public interface CommandEncoderInjection {
    default void clearStencilTexture(GpuTexture texture, int value) {
        throw KiltHelper.createMixinException(CommandEncoderInjection.class, "clearStencilTexture");
    }
}
