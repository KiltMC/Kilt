// TRACKED HASH: 3b77db42b7d2f61c9817130edf60ed8ce3cf62ce
package xyz.bluspring.kilt.forgeinjects.world.level.chunk.storage;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
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