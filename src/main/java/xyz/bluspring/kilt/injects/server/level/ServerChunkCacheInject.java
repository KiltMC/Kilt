package xyz.bluspring.kilt.injects.server.level;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModAbsent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.server.level.ServerChunkCacheInjection;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.storage.SavedDataStorage;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheInject implements ServerChunkCacheInjection {
    @Shadow @Nullable protected abstract ChunkHolder getVisibleChunkIfPresent(long key);

    @ModifyExpressionValue(method = "<init>", at = @At(value = "NEW", target = "(Ljava/nio/file/Path;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/level/storage/SavedDataStorage;"))
    private SavedDataStorage kilt$attachLevelToStorage(SavedDataStorage original, @Local(argsOnly = true, name = "level") ServerLevel level) {
        original.kilt$setLevel(level);
        return original;
    }

    @IfModAbsent("lithium")
    @Definition(id = "profiler", local = @Local(type = ProfilerFiller.class, name = "profiler"))
    @Definition(id = "incrementCounter", method = "Lnet/minecraft/util/profiling/ProfilerFiller;incrementCounter(Ljava/lang/String;)V")
    @Expression("profiler.incrementCounter('getChunkCacheMiss')")
    @Inject(method = "getChunk", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
    private void kilt$bypassFutureChain(int x, int z, ChunkStatus targetStatus, boolean loadOrGenerate, CallbackInfoReturnable<ChunkAccess> cir) {
        var chunkHolder = this.getVisibleChunkIfPresent(ChunkPos.pack(x, z));
        if (chunkHolder != null && chunkHolder.kilt$getCurrentlyLoading() != null) {
            cir.setReturnValue(chunkHolder.kilt$getCurrentlyLoading());
        }
    }

    @Inject(method = "getChunkNow", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkHolder;getChunkIfPresent(Lnet/minecraft/world/level/chunk/status/ChunkStatus;)Lnet/minecraft/world/level/chunk/ChunkAccess;"), cancellable = true)
    private void kilt$bypassFutureChain(int x, int z, CallbackInfoReturnable<LevelChunk> cir, @Local ChunkHolder chunkHolder) {
        var currentlyLoading = chunkHolder.kilt$getCurrentlyLoading();
        if (currentlyLoading != null) {
            cir.setReturnValue(currentlyLoading);
        }
    }
}
