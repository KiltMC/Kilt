package xyz.bluspring.kilt.compat.forge.mixin.ldlib;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.lowdragmc.lowdraglib.client.model.forge.LDLRendererModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.renderer.block.ModelBlockRendererInjection;

@Mixin(value = BlockRenderDispatcher.class, priority = 1500)
public abstract class BlockRenderDispatcherMixin {
    @TargetHandler(mixin = "xyz.bluspring.kilt.forgeinjects.client.renderer.block.BlockRenderDispatcherInject", name = "kilt$tryUseForgeTesselate", prefix = "wrapOperation")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void kilt$injectStateToModelLocation(ModelBlockRenderer instance, BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, RandomSource randomSource, long l, int i, Operation<Void> original, CallbackInfo ci) {
        if (bakedModel instanceof LDLRendererModel.RendererBakedModel model) {
            var blockEntity = blockAndTintGetter.getBlockEntity(blockPos);
            ((ModelBlockRendererInjection) instance).tesselateBlock(blockAndTintGetter, model, blockState, blockPos, poseStack, vertexConsumer, bl, randomSource, l, i, model.getModelData(blockAndTintGetter, blockPos, blockState, blockEntity == null ? ModelData.EMPTY : blockEntity.getModelData()), null);
            ci.cancel();
        }
    }
}
