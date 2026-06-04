package xyz.bluspring.kilt.compat.fabric.mixin.sable;

import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.ryanhcode.sable.fabric.platform.SableSubLevelRenderPlatformImpl;
import dev.ryanhcode.sable.neoforge.compatibility.flywheel.FlywheelCompatNeoForge;
import dev.ryanhcode.sable.sublevel.render.vanilla.SingleBlockSubLevelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@IfModLoaded("sable")
@Mixin(SableSubLevelRenderPlatformImpl.class)
public abstract class SableSubLevelRenderPlatformImplMixin {
    @WrapOperation(method = "tesselateBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;tesselateWithoutAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JI)V"))
    private void kilt$sable$tryTesselateUsingNeo(ModelBlockRenderer instance, BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, long seed, int packedOverlay, Operation<Void> original, @Local(argsOnly = true) RenderType renderType) {
        var modelData = model.getModelData(level, pos, state, level.getModelData(pos));

        if (modelData != ModelData.EMPTY || renderType != null) {
            instance.tesselateWithoutAO(level, model, state, pos, poseStack, consumer, checkSides, random, seed, packedOverlay, modelData, renderType);
        } else {
            original.call(instance, level, model, state, pos, poseStack, consumer, checkSides, random, seed, packedOverlay);
        }
    }

    @ModifyReturnValue(method = "getRenderLayers", at = @At("RETURN"))
    private List<RenderType> kilt$sable$tryGetRenderTypes(List<RenderType> original, SingleBlockSubLevelWrapper blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos pos, RandomSource randomSource) {
        var modelData = blockAndTintGetter.getModelData(pos);
        var renderTypes = bakedModel.getRenderTypes(blockState, randomSource, modelData).asList();
        if (modelData != ModelData.EMPTY || !original.equals(renderTypes)) {
            return renderTypes;
        }

        return original;
    }

    // Kilt TODO: remove this when Sable Fabric has Flywheel support
    @Inject(method = "tryAddFlywheelVisual", at = @At("TAIL"))
    private void kilt$sable$addFlywheelVisual(BlockEntity blockEntity, CallbackInfo ci) {
        FlywheelCompatNeoForge.tryAddVisual(blockEntity);
    }
}
