package xyz.bluspring.kilt.mixin.compat.porting_lib;

import com.mojang.datafixers.util.Pair;
import io.github.fabricators_of_create.porting_lib.chunk.loading.PortingLibChunkManager;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(value = PortingLibChunkManager.TicketHelper.class, remap = false)
public interface TicketHelperAccessor {
    @Accessor
    Map<BlockPos, Pair<LongSet, LongSet>> getBlockTickets();

    @Accessor
    Map<UUID, Pair<LongSet, LongSet>> getEntityTickets();

    @Accessor
    ForcedChunksSavedData getSaveData();

    @Accessor
    String getModId();
}
