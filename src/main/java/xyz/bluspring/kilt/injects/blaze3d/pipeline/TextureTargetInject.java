package xyz.bluspring.kilt.injects.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.blaze3d.pipeline.RenderTargetInjection;

@Mixin(TextureTarget.class)
public abstract class TextureTargetInject extends RenderTarget implements RenderTargetInjection {
    public TextureTargetInject(final @Nullable String label, final int width, final int height, final boolean useDepth) {
        super(label, useDepth);
    }

    @CreateInitializer
    public TextureTargetInject(final @Nullable String label, final int width, final int height, final boolean useDepth, boolean enableStencil) {
        this(label, width, height, useDepth);
        this.kilt$setStencil(enableStencil);

        if (enableStencil)
            this.resize(width, height); // Kilt: resize again to make sure we're using stencils.
    }
}
