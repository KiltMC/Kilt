package xyz.bluspring.kilt.injections.world.level;

import io.github.fabricators_of_create.porting_lib.chunk.loading.extensions.ForcedChunksSavedDataExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.UUID;

@FabricInjectedInterface(ForcedChunksSavedData.class)
public interface ForcedChunksSavedDataInjection extends ForcedChunksSavedDataExtension {
    default ForcedChunkManager.TicketTracker<BlockPos> neo$getBlockForcedChunks() {
        throw KiltHelper.createMixinException(ForcedChunksSavedDataInjection.class, "neo$getBlockForcedChunks");
    }

    default ForcedChunkManager.TicketTracker<UUID> neo$getEntityForcedChunks() {
        throw KiltHelper.createMixinException(ForcedChunksSavedDataInjection.class, "neo$getEntityForcedChunks");
    }
}
