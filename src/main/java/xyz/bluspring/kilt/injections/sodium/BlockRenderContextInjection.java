package xyz.bluspring.kilt.injections.sodium;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.model.data.ModelData;

public interface BlockRenderContextInjection {
    default ModelData kilt$getModelData(BlockPos pos) {
        return ModelData.EMPTY;
    }
}
