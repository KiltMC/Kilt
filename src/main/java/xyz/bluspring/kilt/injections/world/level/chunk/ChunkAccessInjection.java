package xyz.bluspring.kilt.injections.world.level.chunk;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

import javax.annotation.Nullable;

@FabricInjectedInterface(ChunkAccess.class)
public interface ChunkAccessInjection {
    @Nullable
    default Level getLevel() {
        return null;
    }
}
