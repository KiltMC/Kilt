package xyz.bluspring.kilt.injects.blaze3d.systems;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassBackend;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.blaze3d.systems.RenderPassInjection;

@Mixin(RenderPass.class)
public abstract class RenderPassInject implements RenderPassInjection {
    @Shadow @Final private RenderPassBackend backend;

    @Override
    public void setViewport(int x, int y, int width, int height) {
        this.backend.setViewport(x, y, width, height);
    }
}
