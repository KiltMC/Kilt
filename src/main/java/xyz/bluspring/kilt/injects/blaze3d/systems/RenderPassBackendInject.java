package xyz.bluspring.kilt.injects.blaze3d.systems;

import com.mojang.blaze3d.systems.RenderPassBackend;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.blaze3d.systems.RenderPassBackendInjection;

@Mixin(RenderPassBackend.class)
public interface RenderPassBackendInject extends RenderPassBackendInjection {
    @Override
    default void setViewport(int x, int y, int width, int height) {
    }
}
