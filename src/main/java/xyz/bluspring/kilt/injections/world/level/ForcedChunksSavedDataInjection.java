package xyz.bluspring.kilt.injections.world.level;

import io.github.fabricators_of_create.porting_lib.chunk.loading.extensions.ForcedChunksSavedDataExtension;
import net.minecraft.world.level.ForcedChunksSavedData;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(ForcedChunksSavedData.class)
public interface ForcedChunksSavedDataInjection extends ForcedChunksSavedDataExtension {
}
