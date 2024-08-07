package xyz.bluspring.kilt.injections.sodium;

import net.minecraftforge.client.model.data.ModelData;

public interface BlockRenderContextInjection {

    default void kilt$setModelData(ModelData data) {}

    default ModelData kilt$getModelData() {
        return ModelData.EMPTY;
    }
}
