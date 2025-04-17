package xyz.bluspring.kilt.forgeinjects.client.renderer.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.MatrixUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ItemDecoratorHandler;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererInject {
    @Shadow @Final private BlockEntityWithoutLevelRenderer blockEntityRenderer;

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableDepthTest()V", ordinal = 1), method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", locals = LocalCapture.CAPTURE_FAILHARD)
    public void kilt$renderForgeItemDecorator(Font font, ItemStack itemStack, int i, int j, String string, CallbackInfo ci, PoseStack poseStack, LocalPlayer localPlayer, float f, Tesselator tesselator2, BufferBuilder bufferBuilder2) {
        ItemDecoratorHandler.of(itemStack).render(font, itemStack, i, j, f);
    }

    @Shadow public static VertexConsumer getCompassFoilBuffer(MultiBufferSource buffer, RenderType renderType, PoseStack.Pose matrixEntry) { throw new IllegalStateException(); };
    @Shadow public static VertexConsumer getCompassFoilBufferDirect(MultiBufferSource buffer, RenderType renderType, PoseStack.Pose matrixEntry) { throw new IllegalStateException(); };
    @Shadow public static VertexConsumer getFoilBuffer(MultiBufferSource buffer, RenderType renderType, boolean isItem, boolean glint) { throw new IllegalStateException(); };
    @Shadow public static VertexConsumer getFoilBufferDirect(MultiBufferSource buffer, RenderType renderType, boolean noEntity, boolean withGlint) { throw new IllegalStateException(); };
    @Shadow private static boolean hasAnimatedTexture(ItemStack stack) { throw new IllegalStateException(); };

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/ItemTransform;apply(ZLcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void kilt$applyCustomCameraTransforms(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci, @Local(argsOnly = true) LocalRef<BakedModel> modelRef) {
        modelRef.set(ForgeHooksClient.handleCameraTransforms(poseStack, modelRef.get(), displayContext, leftHand));
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getCooldowns()Lnet/minecraft/world/item/ItemCooldowns;", shift = At.Shift.AFTER), method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", locals = LocalCapture.CAPTURE_FAILHARD)
    public void kilt$renderForgeItemDecorator(Font font, ItemStack itemStack, int i, int j, String string, CallbackInfo ci, PoseStack poseStack, LocalPlayer localPlayer) {
        // need to redo it. there might be a better way of doing this.
        var f = localPlayer == null ? 0F : localPlayer.getCooldowns().getCooldownPercent(itemStack.getItem(), Minecraft.getInstance().getFrameTime());

        if (f <= 0.0F)
            ItemDecoratorHandler.of(itemStack).render(font, itemStack, i, j, f);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    private void kilt$tryRenderMultipleLayers(ItemRenderer instance, BakedModel model, ItemStack stack, int combinedLight, int combinedOverlay, PoseStack poseStack, VertexConsumer originalBuffer, Operation<Void> original, @Local RenderType originalRenderType, @Local(ordinal = 2) boolean isFabulous, @Local(argsOnly = true) MultiBufferSource bufferSource, @Local(argsOnly = true) ItemDisplayContext displayContext) {
        var renderPasses = model.getRenderPasses(stack, isFabulous);
        var renderTypes = model.getRenderTypes(stack, isFabulous);

        // Kilt: Detect and fallback to original rendering, for improved mod compatibility.
        if (renderPasses.size() == 1 && renderPasses.get(0) == model && renderTypes.size() == 1 && renderTypes.get(0) == originalRenderType) {
            original.call(instance, model, stack, combinedLight, combinedOverlay, poseStack, originalBuffer);
            return;
        }

        for (BakedModel renderPass : renderPasses) {
            for (RenderType renderType : renderTypes) {
                // TODO: can we avoid copy pasting this entire thing for improved mod compat?
                VertexConsumer vertexConsumer;
                if (hasAnimatedTexture(stack) && stack.hasFoil()) {
                    poseStack.pushPose();
                    PoseStack.Pose pose = poseStack.last();
                    if (displayContext == ItemDisplayContext.GUI) {
                        MatrixUtil.mulComponentWise(pose.pose(), 0.5F);
                    } else if (displayContext.firstPerson()) {
                        MatrixUtil.mulComponentWise(pose.pose(), 0.75F);
                    }

                    if (isFabulous) {
                        vertexConsumer = getCompassFoilBufferDirect(bufferSource, renderType, pose);
                    } else {
                        vertexConsumer = getCompassFoilBuffer(bufferSource, renderType, pose);
                    }

                    poseStack.popPose();
                } else if (isFabulous) {
                    vertexConsumer = getFoilBufferDirect(bufferSource, renderType, true, stack.hasFoil());
                } else {
                    vertexConsumer = getFoilBuffer(bufferSource, renderType, true, stack.hasFoil());
                }

                instance.renderModelLists(renderPass, stack, combinedLight, combinedOverlay, poseStack, vertexConsumer);
            }
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BlockEntityWithoutLevelRenderer;renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    public void kilt$useCustomBlockEntityRenderer(BlockEntityWithoutLevelRenderer instance, ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, Operation<Void> original) {
        var extension = IClientItemExtensions.of(stack);

        if (extension == IClientItemExtensions.DEFAULT) {
            original.call(instance, stack, transformType, poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        var renderer = extension.getCustomRenderer();
        renderer.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
    }

    // This wouldn't be accessible by Kilt, but that's okay.
    // Forge mods would see this.
    public BlockEntityWithoutLevelRenderer getBlockEntityRenderer() {
        return this.blockEntityRenderer;
    }
}
