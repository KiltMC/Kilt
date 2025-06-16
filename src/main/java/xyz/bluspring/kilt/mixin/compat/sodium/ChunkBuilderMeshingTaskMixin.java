package xyz.bluspring.kilt.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.sodium.BlockRenderContextInjection;

@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public class ChunkBuilderMeshingTaskMixin {
    @Shadow @Final private RenderSection render;

    @WrapOperation(
            method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(value = "NEW", target = "(Lme/jellysquid/mods/sodium/client/world/WorldSlice;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;")
    )
    private BlockRenderContext kilt$setChunkPos(WorldSlice world, Operation<BlockRenderContext> original) {
        var renderContext = original.call(world);
        ((BlockRenderContextInjection) renderContext).kilt$setChunkPos(this.render.getChunkX(), this.render.getChunkZ());
        return renderContext;
    }
}
