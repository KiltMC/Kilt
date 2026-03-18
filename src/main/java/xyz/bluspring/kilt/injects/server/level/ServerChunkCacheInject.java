package xyz.bluspring.kilt.injects.server.level;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.server.level.ServerChunkCacheInjection;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheInject implements ServerChunkCacheInjection {
    @Inject(method = "getChunkNow", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;getChunkIfPresent(Lnet/minecraft/world/level/chunk/status/ChunkStatus;)Lnet/minecraft/world/level/chunk/ChunkAccess;"), cancellable = true)
    private void kilt$bypassFutureChain(int chunkX, int chunkZ, CallbackInfoReturnable<LevelChunk> cir, @Local ChunkHolder chunkHolder) {
        var currentlyLoading = chunkHolder.kilt$getCurrentlyLoading();
        if (currentlyLoading != null) {
            cir.setReturnValue(currentlyLoading);
        }
    }

    // Kilt: addRegionTicket and removeRegionTicket handled by Porting Lib
}
