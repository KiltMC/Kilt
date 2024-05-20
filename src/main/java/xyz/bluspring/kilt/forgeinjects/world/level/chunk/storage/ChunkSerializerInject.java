// TRACKED HASH: 3b77db42b7d2f61c9817130edf60ed8ce3cf62ce
package xyz.bluspring.kilt.forgeinjects.world.level.chunk.storage;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkDataEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.level.chunk.LevelChunkInjection;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerInject {
    @Shadow @Final private static Logger LOGGER;

    @WrapOperation(method = "read", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)Lnet/minecraft/world/level/chunk/LevelChunk;"))
    private static LevelChunk kilt$readCaps(Level level, ChunkPos pos, UpgradeData data, LevelChunkTicks blockTicks, LevelChunkTicks fluidTicks, long inhabitedTime, LevelChunkSection[] sections, LevelChunk.PostLoadProcessor postLoad, BlendingData blendingData, Operation<LevelChunk> original, @Local(argsOnly = true) CompoundTag tag) {
        var chunkAccess = original.call(level, pos, data, blockTicks, fluidTicks, inhabitedTime, sections, postLoad, blendingData);

        if (tag.contains("ForgeCaps"))
            ((LevelChunkInjection) chunkAccess).readCapsFromNBT(tag.getCompound("ForgeCaps"));

        return chunkAccess;
    }

    @WrapOperation(method = "read", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/chunk/LevelChunk;Z)Lnet/minecraft/world/level/chunk/ImposterProtoChunk;"))
    private static ImposterProtoChunk kilt$callForgeChunkDataLoadEventProto(LevelChunk wrapped, boolean allowWrites, Operation<ImposterProtoChunk> original, @Local(argsOnly = true) CompoundTag tag, @Local ChunkStatus.ChunkType type) {
        MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Load(wrapped, tag, type));
        return original.call(wrapped, allowWrites);
    }

    @Inject(method = "read", at = @At(value = "RETURN", ordinal = 1))
    private static void kilt$callForgeChunkDataLoadEvent(ServerLevel level, PoiManager poiManager, ChunkPos pos, CompoundTag tag, CallbackInfoReturnable<ProtoChunk> cir, @Local ChunkAccess chunkAccess, @Local ChunkStatus.ChunkType chunkType) {
        MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Load(chunkAccess, tag, chunkType));
    }

    @Inject(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/ChunkSerializer;saveTicks(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/chunk/ChunkAccess$TicksToSave;)V", shift = At.Shift.BEFORE))
    private static void kilt$writeCapsIfLevelChunk(ServerLevel level, ChunkAccess chunk, CallbackInfoReturnable<CompoundTag> cir, @Local CompoundTag tag) {
        if (chunk.getStatus().getChunkType() != ChunkStatus.ChunkType.PROTOCHUNK && chunk instanceof LevelChunk levelChunk) {
            try {
                var capTag = ((LevelChunkInjection) levelChunk).writeCapsToNBT();
                if (capTag != null)
                    tag.put("ForgeCaps", capTag);
            } catch (Exception e) {
                LOGGER.error("A capability provider has thrown an exception trying to write state. It will not persist. Report this to the mod author", e);
            }
        }
    }
}