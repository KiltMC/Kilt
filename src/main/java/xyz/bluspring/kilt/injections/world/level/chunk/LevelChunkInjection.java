package xyz.bluspring.kilt.injections.world.level.chunk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;

public interface LevelChunkInjection extends ICapabilityProviderImpl<LevelChunk> {
    CompoundTag writeCapsToNBT();
    void readCapsFromNBT(CompoundTag tag);
}
