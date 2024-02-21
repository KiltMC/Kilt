package xyz.bluspring.kilt.mixin.porting_lib;

import io.github.fabricators_of_create.porting_lib.chunk.loading.PortingLibChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = PortingLibChunkManager.TicketOwner.class, remap = false)
public interface TicketOwnerAccessor {
    @Invoker(value = "<init>", remap = false)
    static <T extends Comparable<? super T>> PortingLibChunkManager.TicketOwner<T> createTicketOwner(String modId, T owner) {
        throw new UnsupportedOperationException();
    }
}
