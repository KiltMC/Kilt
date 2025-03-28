package xyz.bluspring.kilt.forgeinjects.world.level.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Mixin(ImposterProtoChunk.class)
public abstract class ImposterProtoChunkInject extends ProtoChunk {
    @Shadow @Final private LevelChunk wrapped;

    public ImposterProtoChunkInject(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> biomeRegistry, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, biomeRegistry, blendingData);
    }

    @Override
    public void findBlocks(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> output) {
        this.wrapped.findBlocks(predicate, output);
    }
}
