package xyz.bluspring.kilt.mixin.compat.porting_lib;

import io.github.fabricators_of_create.porting_lib.chunk.loading.PortingLibChunkManager;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(value = PortingLibChunkManager.TicketTracker.class, remap = false)
public interface TicketTrackerAccessor<T extends Comparable<? super T>> {
    @Accessor("chunks")
    Map<PortingLibChunkManager.TicketOwner<T>, LongSet> kilt$getChunks();

    @Accessor("tickingChunks")
    Map<PortingLibChunkManager.TicketOwner<T>, LongSet> kilt$getTickingChunks();

    @Invoker
    boolean callRemove(PortingLibChunkManager.TicketOwner<T> owner, long chunk, boolean ticking);
}
