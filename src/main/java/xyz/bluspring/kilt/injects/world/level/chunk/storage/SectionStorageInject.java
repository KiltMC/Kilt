package xyz.bluspring.kilt.injects.world.level.chunk.storage;

import java.util.Optional;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.level.chunk.storage.SectionStorageInjection;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.storage.SectionStorage;

@Mixin(SectionStorage.class)
public abstract class SectionStorageInject<R> implements SectionStorageInjection {
    @Shadow @Final private Long2ObjectMap<Optional<R>> storage;
    @Shadow @Final private Object loadLock;
    @Shadow @Final protected LevelHeightAccessor levelHeightAccessor;
    @Shadow @Final private LongSet loadedChunks;

    @Shadow
    private static long getKey(ChunkPos chunkPos, int sectionY) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Override
    public void remove(ChunkPos chunkPos) {
        synchronized (this.loadLock) {
            for (int y = this.levelHeightAccessor.getMinSectionY(); y <= this.levelHeightAccessor.getMaxSectionY(); y++) {
                this.storage.remove(getKey(chunkPos, y));
            }

            this.loadedChunks.remove(chunkPos.pack());
        }
    }
}
