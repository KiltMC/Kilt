package xyz.bluspring.kilt.forgeinjects.client.renderer.entity.layers;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.bluspring.kilt.workarounds.ForgeHooksClientWorkaround;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerInject<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @Shadow protected abstract boolean usesInnerModel(EquipmentSlot equipmentSlot);

    @Shadow @Final private static Map<String, ResourceLocation> ARMOR_LOCATION_CACHE;

    protected Model getArmorModelHook(T entity, ItemStack itemStack, EquipmentSlot slot, A model) {
        return ForgeHooksClientWorkaround.getArmorModel(entity, itemStack, slot, model);
    }

    public ResourceLocation getArmorResource(Entity entity, ItemStack stack, EquipmentSlot slot, @Nullable String type) {
        var item = (ArmorItem) stack.getItem();
        var texture = item.getMaterial().getName();
        var domain = "minecraft";

        var idx = texture.indexOf(':');

        if (idx != -1) {
            domain = texture.substring(0, idx);
            texture = texture.substring(idx + 1);
        }

        var path = String.format(Locale.ROOT, "%s:textures/models/armor/%s_layer_%d%s.png",
                domain, texture,
                (this.usesInnerModel(slot) ? 2 : 1),
                type == null ? "" : String.format(Locale.ROOT, "_%s", type)
        );

        path = ForgeHooksClientWorkaround.getArmorTexture(entity, stack, path, slot, type);
        var loc = ARMOR_LOCATION_CACHE.get(path);

        if (loc == null) {
            loc = new ResourceLocation(path);
            ARMOR_LOCATION_CACHE.put(path, loc);
        }

        return loc;
    }

    private void renderModel(PoseStack poseStack, MultiBufferSource source, int i, boolean bl, Model model, float r, float g, float b, ResourceLocation armorResource) {
        var vertexConsumer = ItemRenderer.getArmorFoilBuffer(source, RenderType.armorCutoutNoCull(armorResource), false, bl);
        model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, r, g, b, 1F);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;setPartVisibility(Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/world/entity/EquipmentSlot;)V"), method = "renderArmorPiece", locals = LocalCapture.CAPTURE_FAILHARD)
    private void kilt$getModelHook(PoseStack poseStack, MultiBufferSource multiBufferSource, T livingEntity, EquipmentSlot equipmentSlot, int i, A humanoidModel, CallbackInfo ci, ItemStack itemStack, ArmorItem armorItem, @Share("kilt$model") LocalRef<Model> modelLocalRef) {
        modelLocalRef.set(getArmorModelHook(livingEntity, itemStack, equipmentSlot, humanoidModel));
    }

    @Redirect(at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;ZLnet/minecraft/client/model/HumanoidModel;ZFFFLjava/lang/String;)V"), method = "renderArmorPiece")
    private void kilt$useForgeRenderModel(HumanoidArmorLayer instance, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ArmorItem armorItem, boolean bl, A humanoidModel, boolean bl2, float f, float g, float h, String string, PoseStack _poseStack, MultiBufferSource _multiBufferSource, T livingEntity, EquipmentSlot equipmentSlot, int _i, A _humanoidModel, @Local(ordinal = 0) ItemStack itemStack, @Share("kilt$model") LocalRef<Model> modelLocalRef) {
        this.renderModel(poseStack, multiBufferSource, i, bl, modelLocalRef.get(), f, g, h, this.getArmorResource(livingEntity, itemStack, equipmentSlot, null));
    }

    @Redirect(at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;ZLnet/minecraft/client/model/HumanoidModel;ZFFFLjava/lang/String;)V"), method = "renderArmorPiece")
    private void kilt$useForgeRenderModelWithOverlay(HumanoidArmorLayer instance, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ArmorItem armorItem, boolean bl, A humanoidModel, boolean bl2, float f, float g, float h, String string, PoseStack _poseStack, MultiBufferSource _multiBufferSource, T livingEntity, EquipmentSlot equipmentSlot, int _i, A _humanoidModel, @Local(ordinal = 0) ItemStack itemStack, @Share("kilt$model") LocalRef<Model> modelLocalRef) {
        this.renderModel(poseStack, multiBufferSource, i, bl, modelLocalRef.get(), f, g, h, this.getArmorResource(livingEntity, itemStack, equipmentSlot, "overlay"));
    }

    @Redirect(at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;ZLnet/minecraft/client/model/HumanoidModel;ZFFFLjava/lang/String;)V"), method = "renderArmorPiece")
    private void kilt$useForgeRenderModel2(HumanoidArmorLayer instance, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ArmorItem armorItem, boolean bl, A humanoidModel, boolean bl2, float f, float g, float h, String string, PoseStack _poseStack, MultiBufferSource _multiBufferSource, T livingEntity, EquipmentSlot equipmentSlot, int _i, A _humanoidModel, @Local(ordinal = 0) ItemStack itemStack, @Share("kilt$model") LocalRef<Model> modelLocalRef) {
        this.renderModel(poseStack, multiBufferSource, i, bl, modelLocalRef.get(), f, g, h, this.getArmorResource(livingEntity, itemStack, equipmentSlot, null));
    }
}
