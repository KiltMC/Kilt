package xyz.bluspring.kilt.injects.world.level.chunk.storage;

import java.util.Objects;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.LevelChunkAuxiliaryLightManager;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;

@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerInject {
    @Shadow
    @Final
    private static Logger LOGGER;

    @ModifyExpressionValue(method = "read", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)Lnet/minecraft/world/level/chunk/LevelChunk;"))
    private static LevelChunk kilt$tryLoadNeoLightData(LevelChunk original, @Local(argsOnly = true) CompoundTag tag, @Local(argsOnly = true) ChunkPos pos, @Local(argsOnly = true) ServerLevel level) {
        if (tag.contains(LevelChunkAuxiliaryLightManager.LIGHT_NBT_KEY, Tag.TAG_LIST)) {
            Objects.requireNonNull((LevelChunkAuxiliaryLightManager) original.getAuxLightManager(pos))
                .deserializeNBT(level.registryAccess(), tag.getList(LevelChunkAuxiliaryLightManager.LIGHT_NBT_KEY, Tag.TAG_COMPOUND));
        }

        return original;
    }

    @Inject(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;setLightCorrect(Z)V"))
    private static void kilt$tryLoadNeoAttachmentData(ServerLevel level, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos pos, CompoundTag tag, CallbackInfoReturnable<ProtoChunk> cir, @Local ChunkAccess chunkAccess) {
        if (tag.contains(AttachmentHolder.ATTACHMENTS_NBT_KEY, Tag.TAG_COMPOUND)) {
            chunkAccess.readAttachmentsFromNBT(level.registryAccess(), tag.getCompound(AttachmentHolder.ATTACHMENTS_NBT_KEY));
        }
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void kilt$callLoadChunkDataEvent(ServerLevel level, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos pos, CompoundTag tag, CallbackInfoReturnable<ProtoChunk> cir, @Local ChunkAccess chunkAccess, @Local ChunkType chunkType) {
        NeoForge.EVENT_BUS.post(new ChunkDataEvent.Load(chunkAccess, tag, chunkType));
    }

    @Inject(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/ChunkSerializer;saveTicks(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/chunk/ChunkAccess$TicksToSave;)V"))
    private static void kilt$serializeLightIfLevelChunk(ServerLevel level, ChunkAccess chunk, CallbackInfoReturnable<CompoundTag> cir, @Local ChunkPos chunkPos, @Local CompoundTag tag) {
        if (chunk instanceof LevelChunk levelChunk) {
            Tag lightTag = ((LevelChunkAuxiliaryLightManager) levelChunk.getAuxLightManager(chunkPos)).serializeNBT(level.registryAccess());
            if (lightTag != null)
                tag.put(LevelChunkAuxiliaryLightManager.LIGHT_NBT_KEY, lightTag);
        }
    }

    @Definition(id = "put", method = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;")
    @Expression("?.put('Heightmaps', ?)")
    @Inject(method = "write", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static void kilt$serializeAttachments(ServerLevel level, ChunkAccess chunk, CallbackInfoReturnable<CompoundTag> cir, @Local(ordinal = 0) CompoundTag tag) {
        try {
            final CompoundTag capTag = chunk.writeAttachmentsToNBT(level.registryAccess());
            if (capTag != null) {
                tag.put(AttachmentHolder.ATTACHMENTS_NBT_KEY, capTag);
            }
        } catch (Throwable throwable) {
            LOGGER.error("[Kilt/NeoForge] Failed to write chunk attachments. An attachment has likely thrown an exception while trying to write its state, thus it will not persist. Report this to Kilt!", throwable);
        }
    }
}
