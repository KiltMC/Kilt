package xyz.bluspring.kilt.compat.sodium;

import net.minecraft.client.renderer.RenderType;

public interface ChunkModelRenderTypeHolder {
    RenderType kilt$getRenderType();
    void kilt$setRenderType(RenderType renderType);
}
