package xyz.bluspring.kilt.compat.create.mixin.flywheel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

@IfModLoaded("flywheel")
@Pseudo
@Mixin(targets = "dev.engine_room.flywheel.lib.model.baked.BakedModelBufferer")
public abstract class BakedModelBuffererMixin {
    @Inject(method = "bufferModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;getChunkRenderType(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/RenderType;"))
    private static void kilt$flywheel$tryLoadModelDataSingle(CallbackInfoReturnable<SimpleModel> cir, @Local BlockPos pos, @Local BakedModel model, @Local(argsOnly = true) BlockAndTintGetter level, @Local BlockState state, @Local long seed, @Local RandomSource random,
                                                             @Share("modelData") LocalRef<ModelData> modelData, @Share("renderTypes") LocalRef<ChunkRenderTypeSet> renderTypes) {
        modelData.set(model.getModelData(level, pos, state, level.getModelData(pos)));
        random.setSeed(seed);
        renderTypes.set(model.getRenderTypes(state, random, modelData.get()));
    }

    @Inject(method = "bufferBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;getChunkRenderType(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/RenderType;"))
    private static void kilt$flywheel$tryLoadModelData(CallbackInfoReturnable<SimpleModel> cir, @Local BlockPos pos, @Local BakedModel model, @Local(argsOnly = true) BlockAndTintGetter level, @Local BlockState state, @Local long seed, @Local RandomSource random,
                                                       @Share("modelData") LocalRef<ModelData> modelData, @Share("renderTypes") LocalRef<ChunkRenderTypeSet> renderTypes) {
        modelData.set(model.getModelData(level, pos, state, level.getModelData(pos)));
        random.setSeed(seed);
        renderTypes.set(model.getRenderTypes(state, random, modelData.get()));
    }

    @WrapOperation(method = {"bufferBlocks", "bufferModel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JI)V"))
    private static void kilt$flywheel$tryAddModelDataToTesselate(ModelBlockRenderer instance, BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, long seed, int packedOverlay, Operation<Void> original, @Share("modelData") LocalRef<ModelData> modelData, @Share("renderTypes") LocalRef<ChunkRenderTypeSet> renderTypes, @Local RenderType originalRenderType) {
        if (renderTypes.get().contains(originalRenderType) && renderTypes.get().asList().size() == 1 && modelData.get() == ModelData.EMPTY) {
            original.call(instance, level, model, state, pos, poseStack, consumer, checkSides, random, seed, packedOverlay);
        } else {
            for (RenderType renderType : renderTypes.get()) {
                poseStack.pushPose();
                instance.tesselateBlock(level, model, state, pos, poseStack, consumer, checkSides, random, seed, packedOverlay, modelData.get(), renderType);
                poseStack.popPose();
            }
        }
    }
}
