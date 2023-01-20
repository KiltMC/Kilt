package xyz.bluspring.kilt.forgeinjects.world.level.chunk;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.ChunkAccessInjection;

@Mixin(LevelChunk.class)
public abstract class LevelChunkInject implements ChunkAccessInjection {
    @Shadow public abstract Level getLevel();

    @Nullable
    @Override
    public LevelAccessor getWorldForge() {
        return this.getLevel();
    }
}
