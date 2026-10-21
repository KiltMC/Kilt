package xyz.bluspring.kilt.injections.client.renderer;

import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import net.minecraft.client.renderer.RenderPipelines;

public interface RenderPipelinesInjection {
    static void registerCustomPipelines() {
        ModLoader.postEvent(new RegisterRenderPipelinesEvent(RenderPipelines::register)); // Kilt: let's just register directly into Vanilla, like Fabric does.
    }
}
