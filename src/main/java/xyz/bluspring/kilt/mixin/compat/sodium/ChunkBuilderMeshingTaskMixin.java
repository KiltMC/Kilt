package xyz.bluspring.kilt.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import me.jellysquid.mods.sodium.client.util.task.CancellationToken;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.sodium.BlockRenderContextInjection;

import java.util.Map;

@IfModLoaded("sodium")
@Mixin(ChunkBuilderMeshingTask.class)
public class ChunkBuilderMeshingTaskMixin {
    @Shadow(remap = false) @Final private RenderSection render;

    @Inject(method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At("HEAD"), remap = false)
    private void kilt$getData(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Share("render_data") LocalRef<Map<BlockPos, ModelData>> dataRef) {
        WorldSlice slice = buildContext.cache.getWorldSlice();
        dataRef.set(slice.getModelDataManager().getAt(ChunkPos.asLong(render.getChunkX(), render.getChunkZ())));
    }

    @WrapOperation(method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;update(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/resources/model/BakedModel;J)V"))
    private void kilt$setContext(BlockRenderContext instance, BlockPos pos, BlockPos origin, BlockState state, BakedModel model, long seed, Operation<Void> original, @Share("render_data")LocalRef<Map<BlockPos, ModelData>> dataRef) {
        original.call(instance, pos, origin, state, model, seed);
        ((BlockRenderContextInjection) instance).kilt$setModelData(dataRef.get().get(pos));
    }
}
