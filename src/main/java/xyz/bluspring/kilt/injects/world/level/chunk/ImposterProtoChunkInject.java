package xyz.bluspring.kilt.injects.world.level.chunk;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;

@Mixin(ImposterProtoChunk.class)
public abstract class ImposterProtoChunkInject extends ProtoChunk {
    @Shadow @Final private LevelChunk wrapped;

    public ImposterProtoChunkInject(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, PalettedContainerFactory containerFactory, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, containerFactory, blendingData);
    }

    @Override
    public void findBlocks(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> output) {
        this.wrapped.findBlocks(predicate, output);
    }
}
