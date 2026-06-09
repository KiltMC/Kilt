// TRACKED HASH: cebcc0747792b8bfe53d24573c9609f5c22b61d1
package xyz.bluspring.kilt.injects.server.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.server.level.ChunkMapInjection;
import xyz.bluspring.kilt.mixin.TrackedEntityAccessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(value = ChunkMap.class, priority = 1050)
public abstract class ChunkMapInject implements ChunkMapInjection {
    @Shadow @Final private ServerLevel level;

    @Shadow
    @Final
    private PoiManager poiManager;

    @Shadow
    @Final
    private ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;

    @Shadow
    @Final
    private Int2ObjectMap<ChunkMap.TrackedEntity> entityMap;

    @Inject(method = "updateChunkScheduling", at = @At("TAIL"))
    private void kilt$fireTicketUpdatedEvent(long chunkPos, int newLevel, ChunkHolder holder, int oldLevel, CallbackInfoReturnable<ChunkHolder> cir) {
        if (ChunkLevel.isLoaded(oldLevel) || ChunkLevel.isLoaded(newLevel)) {
            EventHooks.fireChunkTicketLevelUpdated(this.level, chunkPos, oldLevel, newLevel, holder);
        }
    }

    @Inject(method = "method_60440", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setLoaded(Z)V", ordinal = 0, shift = At.Shift.AFTER))
    private void kilt$callChunkUnloadEvent(ChunkHolder chunkHolder, long l, CallbackInfo ci, @Local ChunkAccess chunkAccess) {
        NeoForge.EVENT_BUS.post(new ChunkEvent.Unload(chunkAccess));
    }

    @Inject(method = "method_60440", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ThreadedLevelLightEngine;updateChunkStatus(Lnet/minecraft/world/level/ChunkPos;)V"))
    private void kilt$callAllChunkUnload(ChunkHolder chunkHolder, long l, CallbackInfo ci, @Local ChunkAccess chunkAccess) {
        CommonHooks.onChunkUnload(this.poiManager, chunkAccess);

        // Kilt: don't implement chunk type cache optimization
    }

    @Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;write(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)Ljava/util/concurrent/CompletableFuture;"))
    private void kilt$callChunkSaveEvent(ChunkAccess chunk, CallbackInfoReturnable<Boolean> cir, @Local CompoundTag tag) {
        NeoForge.EVENT_BUS.post(new ChunkDataEvent.Save(chunk, chunk.getLevel() != null ? chunk.getLevel() : this.level, tag));
    }

    @Inject(method = "markChunkPendingToSend(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/chunk/LevelChunk;)V", at = @At("TAIL"))
    private static void kilt$fireChunkWatchEvent(ServerPlayer player, LevelChunk chunk, CallbackInfo ci) {
        EventHooks.fireChunkWatch(player, chunk, player.serverLevel());
    }

    @Inject(method = "dropChunk", at = @At("HEAD"))
    private static void kilt$fireChunkUnwatchEvent(ServerPlayer player, ChunkPos chunkPos, CallbackInfo ci) {
        EventHooks.fireChunkUnWatch(player, chunkPos, player.serverLevel());
    }

    @Definition(id = "entity", local = @Local(type = Entity.class, argsOnly = true))
    @Definition(id = "EnderDragonPart", type = EnderDragonPart.class)
    @Expression("entity instanceof EnderDragonPart")
    @WrapOperation(method = "addEntity", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIfEntityMultipart(Object object, Operation<Boolean> original) {
        return original.call(object) || object instanceof PartEntity<?>;
    }

    @Override
    public List<ServerPlayer> getPlayersWatching(Entity entity) {
        var trackedEntity = this.entityMap.get(entity.getId());

        if (trackedEntity != null) {
            var ret = new ArrayList<ServerPlayer>(((TrackedEntityAccessor) trackedEntity).getSeenBy().size());
            for (ServerPlayerConnection connection : ((TrackedEntityAccessor) trackedEntity).getSeenBy()) {
                ret.add(connection.getPlayer());
            }

            return Collections.unmodifiableList(ret);
        }

        return List.of();
    }

    public void scheduleOnMainThreadMailbox(ChunkTaskPriorityQueueSorter.Message<Runnable> msg) {
        this.mainThreadMailbox.tell(msg);
    }
}
