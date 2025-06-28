package xyz.bluspring.kilt.injections.sodium;

import net.minecraft.core.BlockPos;
import net.minecraftforge.client.model.data.ModelData;

public interface BlockRenderContextInjection {

    default void kilt$setChunkPos(int chunkX, int chunkZ) {}

    default ModelData kilt$getModelData(BlockPos pos) {
        return ModelData.EMPTY;
    }
}
