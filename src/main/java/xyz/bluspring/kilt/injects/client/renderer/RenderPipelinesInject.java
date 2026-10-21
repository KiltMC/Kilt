package xyz.bluspring.kilt.injects.client.renderer;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.renderer.RenderPipelinesInjection;

import net.minecraft.client.renderer.RenderPipelines;

@Mixin(RenderPipelines.class)
public abstract class RenderPipelinesInject {
    @CreateStatic
    private static void registerCustomPipelines() {
        RenderPipelinesInjection.registerCustomPipelines();
    }
}
