package xyz.bluspring.kilt.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import me.jellysquid.mods.sodium.client.render.pipeline.BlockRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.compat.sodium.ChunkModelRenderTypeHolder;

@IfModLoaded("sodium")
@Mixin(ChunkRenderRebuildTask.class)
public abstract class ChunkRenderRebuildTaskMixin {
    @WrapOperation(method = "performBuild", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/pipeline/BlockRenderer;renderModel(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/resources/model/BakedModel;Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;ZJ)Z"), remap = false)
    private boolean kilt$renderModelWithMultiLayer(BlockRenderer instance, BlockAndTintGetter world, BlockState state, BlockPos pos, BlockPos origin, BakedModel model, ChunkModelBuilder originalBuffers, boolean cull, long seed, Operation<Boolean> original, @Local ChunkBuildBuffers buffers, @Local RenderType originalRenderType) {
        var random = RandomSource.create(seed);
        var modelData = world.getModelDataManager().getAt(pos);
        var renderTypes = model.getRenderTypes(state, random, modelData != null ? modelData : ModelData.EMPTY);
        var hasRendered = false;

        if (renderTypes.isEmpty()) {
            return original.call(instance, world, state, pos, origin, model, originalBuffers, cull, seed);
        }

        for (RenderType renderType : renderTypes) {
            var layeredBuffers = buffers.get(renderType);
            ((ChunkModelRenderTypeHolder) layeredBuffers).kilt$setRenderType(renderType);

            if (original.call(instance, world, state, pos, origin, model, layeredBuffers, cull, seed)) {
                hasRendered = true;
            }
        }

        return hasRendered;
    }
}
