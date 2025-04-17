package xyz.bluspring.kilt.forgeinjects.client.renderer.block;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.renderer.block.ModelBlockRendererInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

// higher priority to allow Sodium to function
@Mixin(value = ModelBlockRenderer.class, priority = 1050)
public abstract class ModelBlockRendererInject implements ModelBlockRendererInjection {
    @Shadow public abstract void tesselateWithAO(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, long l, int i);

    @Shadow public abstract void tesselateWithoutAO(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, long l, int i);

    @Shadow public abstract void tesselateBlock(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, long l, int i);

    @Shadow public abstract void renderModel(PoseStack.Pose pose, VertexConsumer vertexConsumer, @Nullable BlockState blockState, BakedModel bakedModel, float f, float g, float h, int i, int j);

    @WrapOperation(method = "tesselateBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    public int kilt$useForgeLightEmission(BlockState instance, Operation<Integer> original, @Local(argsOnly = true) BlockAndTintGetter level, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), BlockState.class, "getLightEmission", BlockAndTintGetter.class, BlockPos.class)) {
            return instance.getLightEmission(level, pos);
        }

        return original.call(instance);
    }

    @WrapOperation(method = "tesselateBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;useAmbientOcclusion()Z"))
    public boolean kilt$useForgeAmbientOcclusion(BakedModel instance, Operation<Boolean> original, @Local(argsOnly = true) BlockState state) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), BakedModel.class, "useAmbientOcclusion", BlockState.class, RenderType.class)) {
            return instance.useAmbientOcclusion(state, kilt$renderType.get());
        }

        return original.call(instance);
    }

    @WrapOperation(method = {"tesselateWithAO", "tesselateWithoutAO", "renderModel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;)Ljava/util/List;"))
    public List<BakedQuad> kilt$getQuadsWithAOOnForgeAtomics(BakedModel instance, @Nullable BlockState state, @Nullable Direction direction, RandomSource randomSource, Operation<List<BakedQuad>> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), BakedModel.class, "getQuads", BlockState.class, Direction.class, RandomSource.class, ModelData.class, RenderType.class)) {
            return instance.getQuads(state, direction, randomSource, kilt$modelData.get(), kilt$renderType.get());
        }

        return original.call(instance, state, direction, randomSource);
    }

    @Inject(at = @At("RETURN"), method = "tesselateBlock")
    public void kilt$resetAtomicsAfterBlockTesselation(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, long l, int i, CallbackInfo ci) {
        kilt$modelData.set(ModelData.EMPTY);
        kilt$renderType.set(null);
    }

    @Inject(at = @At("RETURN"), method = "tesselateWithoutAO")
    public void kilt$resetAtomicsAfterNonAOTesselation(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, long l, int i, CallbackInfo ci) {
        kilt$modelData.set(ModelData.EMPTY);
        kilt$renderType.set(null);
    }

    @Inject(at = @At("RETURN"), method = "tesselateWithAO")
    public void kilt$resetAtomicsAfterAOTesselation(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, long l, int i, CallbackInfo ci) {
        kilt$modelData.set(ModelData.EMPTY);
        kilt$renderType.set(null);
    }

    @Inject(at = @At("RETURN"), method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/resources/model/BakedModel;FFFII)V")
    public void kilt$resetAtomicsAfterModelRender(PoseStack.Pose pose, VertexConsumer vertexConsumer, BlockState blockState, BakedModel bakedModel, float f, float g, float h, int i, int j, CallbackInfo ci) {
        kilt$modelData.set(ModelData.EMPTY);
        kilt$renderType.set(null);
    }

    // Because we can't exactly provide new parameters easily, let's just do this.
    private final AtomicReference<ModelData> kilt$modelData = new AtomicReference<>();
    private final AtomicReference<RenderType> kilt$renderType = new AtomicReference<>();

    @Override
    public void tesselateWithAO(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource random, long l, int i, ModelData modelData, RenderType renderType) {
        kilt$modelData.set(modelData);
        kilt$renderType.set(renderType);
        tesselateWithAO(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, random, l, i);
    }

    @Override
    public void tesselateWithoutAO(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource random, long l, int i, ModelData modelData, RenderType renderType) {
        kilt$modelData.set(modelData);
        kilt$renderType.set(renderType);
        tesselateWithoutAO(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, random, l, i);
    }

    @Override
    public void tesselateBlock(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource random, long l, int i, ModelData modelData, RenderType renderType, boolean queryModelSpecificData) {
        kilt$renderType.set(renderType);
        if (queryModelSpecificData)
            modelData = bakedModel.getModelData(blockAndTintGetter, blockPos, blockState, modelData);
        kilt$modelData.set(modelData);

        tesselateBlock(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, random, l, i);
    }

    @Override
    public void renderModel(PoseStack.Pose pose, VertexConsumer vertexConsumer, @Nullable BlockState blockState, BakedModel bakedModel, float f1, float f2, float f3, int i1, int i2, ModelData modelData, RenderType renderType) {
        kilt$modelData.set(modelData);
        kilt$renderType.set(renderType);
        renderModel(pose, vertexConsumer, blockState, bakedModel, f1, f2, f3, i1, i2);
    }

    // Sodium compatibility
    @IfModLoaded(value = "sodium", maxVersion = "0.6.0")
    @Dynamic
    @TargetHandler(
        mixin = "me.jellysquid.mods.sodium.mixin.features.render.model.block.BlockModelRendererMixin",
        name = "renderFast"
    )
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;)Ljava/util/List;"))
    private List<BakedQuad> kilt$getForgeQuadsDirectional(BakedModel instance, BlockState state, Direction direction, RandomSource randomSource, Operation<List<BakedQuad>> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), BakedModel.class, "getQuads", BlockState.class, Direction.class, RandomSource.class, ModelData.class, RenderType.class)) {
            return instance.getQuads(state, direction, randomSource, kilt$modelData.get(), kilt$renderType.get());
        }

        return original.call(instance, state, direction, randomSource);
    }
}
