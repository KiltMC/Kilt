package xyz.bluspring.kilt.injections.client.renderer.chunk;

import net.minecraftforge.client.model.data.ModelDataManager;

public interface RenderChunkRegionInjection {
    float getShade(float normalX, float normalY, float normalZ, boolean shade);
    ModelDataManager getModelDataManager();
}
