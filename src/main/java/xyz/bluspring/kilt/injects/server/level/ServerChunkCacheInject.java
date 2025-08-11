// TRACKED HASH: d083ce3072cbcec3569d5b9a23634aab7a4b2d83
package xyz.bluspring.kilt.injects.server.level;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.server.level.ChunkHolderInjection;
import xyz.bluspring.kilt.injections.server.level.ServerChunkCacheInjection;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheInject implements ServerChunkCacheInjection {
    @Shadow @Final private DistanceManager distanceManager;

    @Inject(method = "getChunkNow", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;getFutureIfPresent(Lnet/minecraft/world/level/chunk/ChunkStatus;)Ljava/util/concurrent/CompletableFuture;"), cancellable = true)
    private void kilt$checkHasCurrentlyLoading(int chunkX, int chunkZ, CallbackInfoReturnable<LevelChunk> cir, @Local ChunkHolder chunkHolder) {
        if (((ChunkHolderInjection) chunkHolder).kilt$getCurrentlyLoading() != null) {
            cir.setReturnValue(((ChunkHolderInjection) chunkHolder).kilt$getCurrentlyLoading());
        }
    }

    // Handled by Porting Lib
    /*
    @ModifyExpressionValue(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;isNaturalSpawningAllowed(Lnet/minecraft/world/level/ChunkPos;)Z"))
    private boolean kilt$checkCanForceTicks(boolean original) {
        return original || this.distanceManager.shouldForceTicks();
    }

    @Shadow public abstract <T> void addRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object);

    @Shadow public abstract <T> void removeRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object);

    @Override
    public <T> void addRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object, boolean forceTicks) {
        // TODO: actually do something with forceTicks
        this.addRegionTicket(ticketType, chunkPos, i, object);
    }

    @Override
    public <T> void removeRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object, boolean forceTicks) {
        this.removeRegionTicket(ticketType, chunkPos, i, object);
    }*/
}