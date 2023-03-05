package xyz.bluspring.kilt.forgeinjects.client.renderer.entity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ItemDecoratorHandler;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemRenderer.class)
public class ItemRendererInject {
    @Shadow @Final private BlockEntityWithoutLevelRenderer blockEntityRenderer;

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableDepthTest()V", ordinal = 1), method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", locals = LocalCapture.CAPTURE_FAILHARD)
    public void kilt$renderForgeItemDecorator(Font font, ItemStack itemStack, int i, int j, String string, CallbackInfo ci, PoseStack poseStack, LocalPlayer localPlayer, float f, Tesselator tesselator2, BufferBuilder bufferBuilder2) {
        ItemDecoratorHandler.of(itemStack).render(font, itemStack, i, j, f);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getCooldowns()Lnet/minecraft/world/item/ItemCooldowns;", shift = At.Shift.AFTER), method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", locals = LocalCapture.CAPTURE_FAILHARD)
    public void kilt$renderForgeItemDecorator(Font font, ItemStack itemStack, int i, int j, String string, CallbackInfo ci, PoseStack poseStack, LocalPlayer localPlayer) {
        // need to redo it. there might be a better way of doing this.
        var f = localPlayer == null ? 0F : localPlayer.getCooldowns().getCooldownPercent(itemStack.getItem(), Minecraft.getInstance().getFrameTime());

        if (f <= 0.0F)
            ItemDecoratorHandler.of(itemStack).render(font, itemStack, i, j, f);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BlockEntityWithoutLevelRenderer;renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    public void kilt$useCustomBlockEntityRenderer(BlockEntityWithoutLevelRenderer instance, ItemStack itemStack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        var renderer = IClientItemExtensions.of(itemStack).getCustomRenderer();
        renderer.renderByItem(itemStack, transformType, poseStack, multiBufferSource, i, j);
    }

    // This wouldn't be accessible by Kilt, but that's okay.
    // Forge mods would see this.
    public BlockEntityWithoutLevelRenderer getBlockEntityRenderer() {
        return this.blockEntityRenderer;
    }
}
