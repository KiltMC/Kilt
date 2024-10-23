// TRACKED HASH: 709f6c8bf847a0de3a9f176c6e8340ac7efe60ac
package xyz.bluspring.kilt.forgeinjects.client.renderer.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererInject {
    @Shadow @Final private BlockEntityWithoutLevelRenderer blockEntityRenderer;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/ItemTransform;apply(ZLcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void kilt$applyCustomCameraTransforms(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci, @Local(argsOnly = true) LocalRef<BakedModel> modelRef) {
        modelRef.set(ForgeHooksClient.handleCameraTransforms(poseStack, modelRef.get(), displayContext, leftHand));
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BlockEntityWithoutLevelRenderer;renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    public void kilt$useCustomBlockEntityRenderer(BlockEntityWithoutLevelRenderer instance, ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, Operation<Void> original) {
        var extension = IClientItemExtensions.of(stack);

        if (extension == IClientItemExtensions.DEFAULT) {
            original.call(instance, stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        var renderer = extension.getCustomRenderer();
        renderer.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }

    // This wouldn't be accessible by Kilt, but that's okay.
    // Forge mods would see this.
    public BlockEntityWithoutLevelRenderer getBlockEntityRenderer() {
        return this.blockEntityRenderer;
    }
}