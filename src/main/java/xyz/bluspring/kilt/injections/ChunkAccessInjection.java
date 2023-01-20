package xyz.bluspring.kilt.injections;

import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;

public interface ChunkAccessInjection {
    @Nullable
    default LevelAccessor getWorldForge() {
        return null;
    }
}
