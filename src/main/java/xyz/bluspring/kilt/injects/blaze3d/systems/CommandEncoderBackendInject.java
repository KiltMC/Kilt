package xyz.bluspring.kilt.injects.blaze3d.systems;

import com.mojang.blaze3d.systems.CommandEncoderBackend;
import com.mojang.blaze3d.textures.GpuTexture;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.blaze3d.systems.CommandEncoderBackendInjection;

@Mixin(CommandEncoderBackend.class)
public interface CommandEncoderBackendInject extends CommandEncoderBackendInjection {
    @Override
    default void clearStencilTexture(GpuTexture texture, int value) {
    }
}
