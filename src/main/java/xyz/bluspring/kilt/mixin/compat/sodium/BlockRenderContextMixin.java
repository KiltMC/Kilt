package xyz.bluspring.kilt.mixin.compat.sodium;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.sodium.BlockRenderContextInjection;

import java.util.Map;

@IfModLoaded("sodium")
@Mixin(BlockRenderContext.class)
public class BlockRenderContextMixin implements BlockRenderContextInjection {
    private Map<BlockPos, ModelData> kilt$modelData;

    @Override
    public void kilt$setChunkPos(int chunkX, int chunkZ) {
        this.kilt$modelData = net.minecraft.client.Minecraft.getInstance().level.getModelDataManager().getAt(new ChunkPos(chunkX, chunkZ));
    }

    @Override
    public ModelData kilt$getModelData(BlockPos pos) {
        return kilt$modelData.getOrDefault(pos, ModelData.EMPTY);
    }
}
