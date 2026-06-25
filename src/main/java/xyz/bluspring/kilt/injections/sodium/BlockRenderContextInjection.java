package xyz.bluspring.kilt.injections.sodium;

import net.neoforged.neoforge.model.data.ModelData;

import net.minecraft.core.BlockPos;

public interface BlockRenderContextInjection {
    default ModelData kilt$getModelData(BlockPos pos) {
        return ModelData.EMPTY;
    }
}
