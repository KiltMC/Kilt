package xyz.bluspring.kilt.forgeinjects.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.lighting.ForgeModelBlockRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.render.block.ModelBlockRendererInjection;

@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherInject {
    @Shadow @Final @Mutable
    private ModelBlockRenderer modelRenderer;

    @Shadow @Final private BlockModelShaper blockModelShaper;

    @Shadow @Final private RandomSource random;

    @Shadow public abstract BakedModel getBlockModel(BlockState state);

    @Shadow @Final private BlockColors blockColors;

    @Shadow @Final private BlockEntityWithoutLevelRenderer blockEntityRenderer;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void kilt$useForgeModelRenderer(BlockModelShaper blockModelShaper, BlockEntityWithoutLevelRenderer blockEntityWithoutLevelRenderer, BlockColors blockColors, CallbackInfo ci) {
        this.modelRenderer = new ForgeModelBlockRenderer(blockColors);
    }

    public void renderBreakingTexture(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack poseStack, VertexConsumer consumer, ModelData data) {
        if (state.getRenderShape() == RenderShape.MODEL) {
            var bakedModel = this.blockModelShaper.getBlockModel(state);
            var seed = state.getSeed(pos);
            ((ModelBlockRendererInjection) this.modelRenderer).tesselateBlock(level, bakedModel, state, pos, poseStack, consumer, true, this.random, seed, OverlayTexture.NO_OVERLAY, data, null);
        }
    }

    public void renderBatched(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, ModelData modelData, RenderType renderType) {
        renderBatched(state, pos, level, poseStack, consumer, checkSides, random, modelData, renderType, true);
    }

    public void renderBatched(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack poseStack, VertexConsumer consumer, boolean checkSides, RandomSource random, ModelData modelData, RenderType renderType, boolean queryModelSpecificData) {
        try {
            RenderShape renderShape = state.getRenderShape();
            if (renderShape == RenderShape.MODEL) {
                ((ModelBlockRendererInjection) this.modelRenderer).tesselateBlock(level, this.getBlockModel(state), state, pos, poseStack, consumer, checkSides, random, state.getSeed(pos), OverlayTexture.NO_OVERLAY, modelData, renderType, queryModelSpecificData);
            }

        } catch (Throwable var11) {
            CrashReport crashReport = CrashReport.forThrowable(var11, "Tesselating block in world");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(crashReportCategory, level, pos, state);
            throw new ReportedException(crashReport);
        }
    }

    public void renderSingleBlock(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, ModelData modelData, RenderType renderType) {
        RenderShape renderShape = state.getRenderShape();
        if (renderShape != RenderShape.INVISIBLE) {
            switch (renderShape) {
                case MODEL -> {
                    BakedModel bakedModel = this.getBlockModel(state);
                    int i = this.blockColors.getColor(state, null, null, 0);
                    float f = (float) (i >> 16 & 255) / 255.0F;
                    float g = (float) (i >> 8 & 255) / 255.0F;
                    float h = (float) (i & 255) / 255.0F;
                    for (RenderType type : bakedModel.getRenderTypes(state, RandomSource.create(42), modelData)) {
                        ((ModelBlockRendererInjection) this.modelRenderer).renderModel(poseStack.last(), bufferSource.getBuffer(renderType != null ? renderType : RenderTypeHelper.getEntityRenderType(type, false)), state, bakedModel, f, g, h, packedLight, packedOverlay, modelData, type);
                    }
                }
                case ENTITYBLOCK_ANIMATED -> {
                    ItemStack stack = new ItemStack(state.getBlock());
                    IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, ItemTransforms.TransformType.NONE, poseStack, bufferSource, packedLight, packedOverlay);
                }
            }

        }
    }
}
