package xyz.bluspring.kilt.injections.sodium;

import net.minecraftforge.client.model.data.ModelData;

public interface BlockRenderContextInjection {

    default void kilt$set(ModelData data) {}

    default ModelData klit$data() {
        return ModelData.EMPTY;
    }
}
