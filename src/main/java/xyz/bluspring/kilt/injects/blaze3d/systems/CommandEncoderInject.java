package xyz.bluspring.kilt.injects.blaze3d.systems;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.CommandEncoderBackend;
import com.mojang.blaze3d.textures.GpuTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.injections.blaze3d.systems.CommandEncoderInjection;

@Mixin(CommandEncoder.class)
public abstract class CommandEncoderInject implements CommandEncoderInjection {
    @Shadow @Final private CommandEncoderBackend backend;

    @Override
    public void clearStencilTexture(GpuTexture texture, int value) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        } else {
            this.verifyStencilTexture(texture);
            this.backend.clearStencilTexture(texture, value);
        }
    }

    @Unique
    private void verifyStencilTexture(GpuTexture stencilTexture) {
        if (!stencilTexture.getFormat().hasStencilAspect()) {
            throw new IllegalStateException("Trying to clear stencil in a texture that has no stencil component!");
        } else if (stencilTexture.isClosed()) {
            throw new IllegalStateException("Stencil texture is closed");
        } else if ((stencilTexture.usage() & GpuTexture.USAGE_RENDER_ATTACHMENT) == 0) {
            throw new IllegalStateException("Stencil texture must have USAGE_RENDER_ATTACHMENT");
        } else if ((stencilTexture.usage() & GpuTexture.USAGE_COPY_DST) == 0) {
            throw new IllegalStateException("Stencil texture must have USAGE_COPY_DST");
        }
    }
}
