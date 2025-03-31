package xyz.bluspring.kilt.forgeinjects.server.level;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.server.level.ChunkHolderInjection;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkMap.class)
public abstract class ChunkMapInject {
    @Shadow @Final private ServerLevel level;

    @Inject(method = "updateChunkScheduling", at = @At(value = "RETURN", ordinal = 1))
    private void kilt$fireChunkTicketUpdated(long chunkPos, int newLevel, ChunkHolder holder, int oldLevel, CallbackInfoReturnable<ChunkHolder> cir) {
        ForgeEventFactory.fireChunkTicketLevelUpdated(this.level, chunkPos, oldLevel, newLevel, holder);
    }

    @Inject(method = "method_18843", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setLoaded(Z)V", shift = At.Shift.AFTER))
    private void kilt$callChunkUnloadEvent(ChunkHolder chunkHolder, CompletableFuture completableFuture, long l, ChunkAccess chunkAccess, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new ChunkEvent.Unload(chunkAccess));
    }

    @Inject(method = "method_17227", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;registerAllBlockEntitiesAfterLevelLoad()V"))
    private void kilt$markChunkLoading(ChunkHolder chunkHolder, ChunkAccess chunkAccess, CallbackInfoReturnable<ChunkAccess> cir, @Local LevelChunk chunk) {
        ((ChunkHolderInjection) chunkHolder).kilt$setCurrentlyLoading(chunk);
    }

    @Inject(method = "method_17227", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;registerTickContainerInLevel(Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER))
    private void kilt$callChunkLoadEvent(ChunkHolder chunkHolder, ChunkAccess chunkAccess, CallbackInfoReturnable<ChunkAccess> cir) {
        MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunkAccess));
    }

    @WrapMethod(method = "method_17227")
    private ChunkAccess kilt$wrapInTryFinally(ChunkHolder chunkHolder, ChunkAccess chunkAccess, Operation<ChunkAccess> original) {
        try {
            return original.call(chunkHolder, chunkAccess);
        } finally {
            ((ChunkHolderInjection) chunkHolder).kilt$setCurrentlyLoading(null);
        }
    }

    @Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;write(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)V"))
    private void kilt$callChunkSaveData(ChunkAccess chunk, CallbackInfoReturnable<Boolean> cir, @Local CompoundTag tag) {
        MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Save(chunk, chunk.getWorldForge() != null ? chunk.getWorldForge() : this.level, tag));
    }

    @Inject(method = "updateChunkTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;untrackChunk(Lnet/minecraft/world/level/ChunkPos;)V", shift = At.Shift.AFTER))
    private void kilt$fireChunkUnwatch(ServerPlayer player, ChunkPos chunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> packetCache, boolean wasLoaded, boolean load, CallbackInfo ci) {
        ForgeEventFactory.fireChunkUnWatch(player, chunkPos, this.level);
    }

    @WrapOperation(method = "addEntity", constant = @Constant(classValue = EnderDragonPart.class))
    private boolean kilt$checkIsPartEntity(Object object, Operation<Boolean> original) {
        return original.call(object) || original instanceof PartEntity<?>;
    }

    @Inject(method = "playerLoadedChunk", at = @At("TAIL"))
    private void kilt$fireChunkWatch(ServerPlayer player, MutableObject<ClientboundLevelChunkWithLightPacket> packetCache, LevelChunk chunk, CallbackInfo ci) {
        ForgeEventFactory.fireChunkWatch(player, chunk, this.level);
    }
}
