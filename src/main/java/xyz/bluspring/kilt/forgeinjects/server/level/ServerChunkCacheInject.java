package xyz.bluspring.kilt.forgeinjects.server.level;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.server.level.ChunkHolderInjection;
import xyz.bluspring.kilt.injections.server.level.ServerChunkCacheInjection;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheInject implements ServerChunkCacheInjection {
    @Shadow public abstract <T> void addRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object);

    @Shadow public abstract <T> void removeRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object);

    @Inject(method = "getChunkNow", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;getFutureIfPresent(Lnet/minecraft/world/level/chunk/ChunkStatus;)Ljava/util/concurrent/CompletableFuture;"), cancellable = true)
    private void kilt$avoidDeadlockIfLoading(int chunkX, int chunkZ, CallbackInfoReturnable<LevelChunk> cir, @Local ChunkHolder chunkHolder) {
        if (((ChunkHolderInjection) chunkHolder).kilt$getCurrentlyLoading() != null)
            cir.setReturnValue(((ChunkHolderInjection) chunkHolder).kilt$getCurrentlyLoading());
    }

    @Override
    public <T> void addRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object, boolean forceTicks) {
        // TODO: actually do something with forceTicks
        this.addRegionTicket(ticketType, chunkPos, i, object);
    }

    @Override
    public <T> void removeRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object, boolean forceTicks) {
        this.removeRegionTicket(ticketType, chunkPos, i, object);
    }
}
