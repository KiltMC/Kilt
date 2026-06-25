package xyz.bluspring.kilt.compat.create.mixin.ponder_fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.engine_room.flywheel.lib.model.baked.VirtualBlockGetter;
import net.createmod.catnip.client.render.model.ShadeSeparatedBufferSource;
import net.createmod.catnip.impl.client.render.model.BakedModelBuffererImpl;
import net.createmod.ponder.render.VirtualRenderHelper;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

@Pseudo
@Mixin(BakedModelBuffererImpl.class)
public abstract class BakedModelBuffererImplMixin {
    @Inject(method = "bufferModel(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/createmod/catnip/client/render/model/ShadeSeparatedBufferSource;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;getChunkRenderType(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/RenderType;"))
    private static void kilt$flywheel$tryLoadModelDataSingle(CallbackInfo ci, @Local BlockPos pos, @Local BakedModel model, @Local(argsOnly = true) BlockAndTintGetter level, @Local BlockState state, @Local long seed, @Local RandomSource random,
                                                             @Share("modelData") LocalRef<ModelData> modelData, @Share("renderTypes") LocalRef<ChunkRenderTypeSet> renderTypes) {
        modelData.set(level.getModelData(pos));
        if (modelData.get() == ModelData.EMPTY && level instanceof VirtualBlockGetter) {
            modelData.set(VirtualRenderHelper.VIRTUAL_DATA);
        }
        modelData.set(model.getModelData(level, pos, state, modelData.get()));

        random.setSeed(seed);
        renderTypes.set(model.getRenderTypes(state, random, modelData.get()));
    }

    @Inject(method = "bufferBlocks(Ljava/util/Iterator;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;ZLnet/createmod/catnip/client/render/model/ShadeSeparatedBufferSource;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;getChunkRenderType(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/RenderType;"))
    private static void kilt$flywheel$tryLoadModelData(CallbackInfo ci, @Local BlockPos pos, @Local BakedModel model, @Local(argsOnly = true) BlockAndTintGetter level, @Local BlockState state, @Local long seed, @Local RandomSource random,
                                                       @Share("modelData") LocalRef<ModelData> modelData, @Share("renderTypes") LocalRef<ChunkRenderTypeSet> renderTypes) {
        modelData.set(level.getModelData(pos));
        if (modelData.get() == ModelData.EMPTY && level instanceof VirtualBlockGetter) {
            modelData.set(VirtualRenderHelper.VIRTUAL_DATA);
        }
        modelData.set(model.getModelData(level, pos, state, modelData.get()));

        random.setSeed(seed);
        renderTypes.set(model.getRenderTypes(state, random, modelData.get()));
    }

    @WrapOperation(method = {"bufferBlocks(Ljava/util/Iterator;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;ZLnet/createmod/catnip/client/render/model/ShadeSeparatedBufferSource;)V", "bufferModel(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/createmod/catnip/client/render/model/ShadeSeparatedBufferSource;)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JI)V"))
    private static void kilt$flywheel$tryAddModelDataToTesselate(ModelBlockRenderer instance, BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, long seed, int packedOverlay, Operation<Void> original, @Share("modelData") LocalRef<ModelData> modelData, @Share("renderTypes") LocalRef<ChunkRenderTypeSet> renderTypes, @Local RenderType originalRenderType, @Local ShadeSeparatedBufferSource bufferSource) {
        if (renderTypes.get().contains(originalRenderType) && renderTypes.get().asList().size() == 1 && modelData.get() == ModelData.EMPTY) {
            original.call(instance, level, model, state, pos, poseStack, consumer, checkSides, random, seed, packedOverlay);
        } else {
            for (RenderType renderType : renderTypes.get()) {
                ((UniversalMeshEmitterAccessor) consumer).kilt$invokePrepare(bufferSource, renderType);
                poseStack.pushPose();
                instance.tesselateBlock(level, model, state, pos, poseStack, consumer, checkSides, random, seed, packedOverlay, modelData.get(), renderType);
                poseStack.popPose();
            }
        }
    }
}
