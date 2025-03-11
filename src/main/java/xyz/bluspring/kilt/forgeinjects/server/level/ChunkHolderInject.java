package xyz.bluspring.kilt.forgeinjects.server.level;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.injections.server.level.ChunkHolderInjection;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderInject implements ChunkHolderInjection {
    @Unique
    LevelChunk currentlyLoading;

    @Override
    public LevelChunk kilt$getCurrentlyLoading() {
        return this.currentlyLoading;
    }

    @Override
    public void kilt$setCurrentlyLoading(LevelChunk chunk) {
        this.currentlyLoading = chunk;
    }
}
