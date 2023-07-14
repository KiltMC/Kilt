package xyz.bluspring.kilt.forgeinjects.client.renderer.chunk;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.extensions.IForgeBlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.mixin.RenderChunkAccessor;

import java.util.Map;
import java.util.Set;

@Mixin(ChunkRenderDispatcher.class)
public class ChunkRenderDispatcherInject {

    @Mixin(targets = {"net/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask"})
    public static class RebuildTaskInject {
        protected Map<BlockPos, ModelData> modelData;
        private ChunkRenderDispatcher.RenderChunk renderChunk;

        @Inject(method = "<init>", at = @At("TAIL"))
        public void kilt$getModelData(ChunkRenderDispatcher.RenderChunk renderChunk, double d, RenderChunkRegion renderChunkRegion, boolean bl, CallbackInfo ci) {
            this.renderChunk = renderChunk;
            this.modelData = ((IForgeBlockGetter) Minecraft.getInstance().level).getModelDataManager().getAt(new ChunkPos(renderChunk.getOrigin()));
        }

        // TODO: Improve this code, there has to be a better way of doing it, right?
        @Redirect(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"))
        public RenderShape kilt$renderWithMultipleRenderTypes(BlockState instance, @Local BlockRenderDispatcher dispatcher, @Local RenderChunkRegion region, @Local(ordinal = 1) BlockPos pos, @Local(ordinal = 2) BlockPos blockPos3, @Local RandomSource randomSource, @Local ChunkBufferBuilderPack chunkBufferBuilderPack, @Local PoseStack poseStack, @Local Set<RenderType> set) {
            if (instance.getRenderShape() != RenderShape.INVISIBLE) {
                var model = dispatcher.getBlockModel(instance);
                var modelData = model.getModelData(region, pos, instance, this.modelData.getOrDefault(pos, ModelData.EMPTY));

                randomSource.setSeed(instance.getSeed(pos));

                for (RenderType renderType : model.getRenderTypes(instance, randomSource, modelData)) {
                    var bufferBuilder = chunkBufferBuilderPack.builder(renderType);
                    if (set.add(renderType)) {
                        ((RenderChunkAccessor) renderChunk).callBeginLayer(bufferBuilder);
                    }

                    poseStack.pushPose();
                    poseStack.translate((blockPos3.getX() & 15), (blockPos3.getY() & 15), (blockPos3.getZ() & 15));
                    dispatcher.renderBatched(instance, blockPos3, region, poseStack, bufferBuilder, true, randomSource);
                    poseStack.popPose();
                }
            }

            // pretty much just cancel it lmao
            return RenderShape.INVISIBLE;
        }
    }
}
