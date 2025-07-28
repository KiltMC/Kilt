package xyz.bluspring.kilt.injections.sodium;

import net.minecraft.core.BlockPos;
import net.minecraftforge.client.model.data.ModelData;

public interface BlockRenderContextInjection {
    default ModelData kilt$getModelData(BlockPos pos) {
        return ModelData.EMPTY;
    }
}
