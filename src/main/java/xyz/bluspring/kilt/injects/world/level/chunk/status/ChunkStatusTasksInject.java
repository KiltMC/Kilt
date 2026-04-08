package xyz.bluspring.kilt.injects.world.level.chunk.status;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;

@Mixin(ChunkStatusTasks.class)
public abstract class ChunkStatusTasksInject {
    @WrapOperation(method = "method_60553", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;runPostLoad()V"))
    private static void bypassFutureChain(LevelChunk instance, Operation<Void> original, @Local(argsOnly = true) GenerationChunkHolder holder) {
        try {
            holder.kilt$setCurrentlyLoading(instance);
            original.call(instance);
        } finally {
            holder.kilt$setCurrentlyLoading(null);
        }
    }

    @WrapOperation(method = "method_60553", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;registerAllBlockEntitiesAfterLevelLoad()V"))
    private static void bypassFutureChain2(LevelChunk instance, Operation<Void> original, @Local(argsOnly = true) GenerationChunkHolder holder) {
        try {
            holder.kilt$setCurrentlyLoading(instance);
            original.call(instance);
        } finally {
            holder.kilt$setCurrentlyLoading(null);
        }
    }

    @WrapOperation(method = "method_60553", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;registerTickContainerInLevel(Lnet/minecraft/server/level/ServerLevel;)V"))
    private static void bypassFutureChain3(LevelChunk instance, ServerLevel arg, Operation<Void> original, @Local(argsOnly = true) GenerationChunkHolder holder, @Local ProtoChunk protoChunk) {
        try {
            holder.kilt$setCurrentlyLoading(instance);
            original.call(instance, arg);
            NeoForge.EVENT_BUS.post(new ChunkEvent.Load(instance, !(protoChunk instanceof ImposterProtoChunk)));
        } finally {
            holder.kilt$setCurrentlyLoading(null);
        }
    }
}
