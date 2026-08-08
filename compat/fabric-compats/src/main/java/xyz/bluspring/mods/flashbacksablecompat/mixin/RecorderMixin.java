package xyz.bluspring.mods.flashbacksablecompat.mixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.flashback.record.Recorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.mods.flashbacksablecompat.ModSupport;
import xyz.bluspring.mods.flashbacksablecompat.compat.SableSupport;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(Recorder.class)
public abstract class RecorderMixin {
    @WrapOperation(method = "writeChunkDataSnapshot", at = @At(value = "NEW", target = "(I)Ljava/util/ArrayList;", ordinal = 0))
    private ArrayList<LevelChunk> addSableChunksToList(int initialCapacity, Operation<ArrayList<LevelChunk>> original, @Local(argsOnly = true) ClientLevel level) {
        List<LevelChunk> sableChunks = ModSupport.SABLE_LOADED ? SableSupport.getSableChunks(level) : Collections.emptyList();
        var list = original.call(initialCapacity + sableChunks.size());
        list.addAll(sableChunks);

        return list;
    }
}
