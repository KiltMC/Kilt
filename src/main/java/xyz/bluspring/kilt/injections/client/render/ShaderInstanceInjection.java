package xyz.bluspring.kilt.injections.client.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;

public interface ShaderInstanceInjection {
    static ShaderInstance create(ResourceProvider resourceProvider, ResourceLocation resourceLocation, VertexFormat vertexFormat) throws IOException {
        return new ShaderInstance(resourceProvider, resourceLocation.toString(), vertexFormat);
    }
}
