// TRACKED HASH: 701e6411dae2a52635a7b1ef1493b50b798afa97
package xyz.bluspring.kilt.injects.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelDataManager;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.renderer.chunk.RenderChunkRegionInjection;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

@Mixin(RenderChunkRegion.class)
public abstract class RenderChunkRegionInject implements BlockAndTintGetter, RenderChunkRegionInjection {
    @Shadow @Final protected Level level;
    @Shadow protected abstract RenderChunk getChunk(int x, int z);

    @Unique private Long2ObjectFunction<ModelData> modelDataSnapshot = ModelDataManager.EMPTY_SNAPSHOT;

    RenderChunkRegionInject(Level level, int minChunkX, int minChunkZ, RenderChunk[] chunks) {
    }

    @CreateInitializer
    RenderChunkRegionInject(Level level, int minChunkX, int minChunkZ, RenderChunk[] chunks, Long2ObjectFunction<ModelData> modelDataSnapshot) {
        this(level, minChunkX, minChunkZ, chunks);
        this.modelDataSnapshot = modelDataSnapshot;
    }

    @Override
    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        return this.level.getShade(normalX, normalY, normalZ, shade);
    }

    @Override
    public ModelData getModelData(BlockPos pos) {
        return this.modelDataSnapshot.get(pos.asLong());
    }

    @Override
    public @Nullable AuxiliaryLightManager getAuxLightManager(ChunkPos pos) {
        return this.getChunk(pos.x, pos.z).wrapped.getAuxLightManager(pos);
    }

    @Override
    public void kilt$setModelDataSnapshot(Long2ObjectFunction<ModelData> modelDataSnapshot) {
        this.modelDataSnapshot = modelDataSnapshot;
    }
}