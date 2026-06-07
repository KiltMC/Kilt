package xyz.bluspring.kilt.injects.client.renderer.entity.layers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.util.KiltHelper;
import xyz.bluspring.kilt.workarounds.RenderLayerData;
import xyz.bluspring.kilt.workarounds.WrappedModelAsHumanoid;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerInject<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @Shadow
    @Final
    private TextureAtlas armorTrimAtlas;
    @Unique private final RenderLayerData kilt$renderLayerData = new RenderLayerData();
    @Unique private final WrappedModelAsHumanoid kilt$wrappedHumanoidModel = new WrappedModelAsHumanoid();

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"))
    private void kilt$updateRenderLayerData(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        this.kilt$renderLayerData.update(limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
    }

    @Inject(method = "renderArmorPiece", at = @At("HEAD"))
    private void kilt$initShareData(PoseStack poseStack, MultiBufferSource bufferSource, T livingEntity, EquipmentSlot slot, int packedLight, A model, CallbackInfo ci,
                                    @Share(value = "limbSwing", namespace = Kilt.MOD_ID) LocalFloatRef limbSwing,
                                    @Share(value = "limbSwingAmount", namespace = Kilt.MOD_ID) LocalFloatRef limbSwingAmount,
                                    @Share(value = "partialTicks", namespace = Kilt.MOD_ID) LocalFloatRef partialTicks,
                                    @Share(value = "ageInTicks", namespace = Kilt.MOD_ID) LocalFloatRef ageInTicks,
                                    @Share(value = "netHeadYaw", namespace = Kilt.MOD_ID) LocalFloatRef netHeadYaw,
                                    @Share(value = "headPitch", namespace = Kilt.MOD_ID) LocalFloatRef headPitch) {
        limbSwing.set(this.kilt$renderLayerData.getLimbSwing());
        limbSwingAmount.set(this.kilt$renderLayerData.getLimbSwingAmount());
        partialTicks.set(this.kilt$renderLayerData.getPartialTicks());
        ageInTicks.set(this.kilt$renderLayerData.getAgeInTicks());
        netHeadYaw.set(this.kilt$renderLayerData.getNetHeadYaw());
        headPitch.set(this.kilt$renderLayerData.getHeadPitch());
    }

    @Inject(method = "renderArmorPiece", at= @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;setPartVisibility(Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/world/entity/EquipmentSlot;)V", shift = At.Shift.AFTER))
    private void kilt$storeCustomArmorModel(PoseStack poseStack, MultiBufferSource bufferSource, T livingEntity, EquipmentSlot slot, int packedLight, A model, CallbackInfo ci, @Share(value = "model", namespace = Kilt.MOD_ID) LocalRef<Model> modelRef, @Local ItemStack stack) {
        modelRef.set(this.getArmorModelHook(livingEntity, stack, slot, model));
    }

    @ModifyVariable(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ArmorMaterial;layers()Ljava/util/List;"), ordinal = 1)
    private int kilt$setupCustomDefaultDyeColor(int original, PoseStack poseStack, MultiBufferSource bufferSource, T livingEntity, EquipmentSlot slot, int packedLight, A model, @Local ItemStack stack, @Share(value = "extensions", namespace = Kilt.MOD_ID) LocalRef<IClientItemExtensions> extensionsRef, @Share(value = "model", namespace = Kilt.MOD_ID) LocalRef<Model> modelRef) {
        extensionsRef.set(IClientItemExtensions.of(stack));
        if (extensionsRef.get() == IClientItemExtensions.DEFAULT)
            return original;

        extensionsRef.get().setupModelAnimations(livingEntity, stack, slot, modelRef.get(), this.kilt$renderLayerData.getLimbSwing(), this.kilt$renderLayerData.getLimbSwingAmount(), this.kilt$renderLayerData.getPartialTicks(), this.kilt$renderLayerData.getAgeInTicks(), this.kilt$renderLayerData.getNetHeadYaw(), this.kilt$renderLayerData.getHeadPitch());
        return extensionsRef.get().getDefaultDyeColor(stack);
    }

    @WrapOperation(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/HumanoidModel;ILnet/minecraft/resources/ResourceLocation;)V"))
    private void kilt$tryRenderModel(HumanoidArmorLayer<T, M, A> instance, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, A humanoidModel, int dyeColor, ResourceLocation resourceLocation, Operation<Void> original, @Share(value = "model", namespace = Kilt.MOD_ID) LocalRef<Model> modelRef, @Share(value = "extensions", namespace = Kilt.MOD_ID) LocalRef<IClientItemExtensions> extensionsRef, @Local ItemStack stack, @Local(argsOnly = true) LivingEntity entity, @Local ArmorMaterial.Layer layer, @Local ArmorMaterial material, @Local boolean flag, @Local(argsOnly = true) EquipmentSlot slot) {
        if (extensionsRef.get() != IClientItemExtensions.DEFAULT || KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), HumanoidArmorLayer.class, "getArmorModelHook", LivingEntity.class, ItemStack.class, EquipmentSlot.class, HumanoidModel.class)) {
            int color = extensionsRef.get().getArmorLayerTintColor(stack, entity, layer, material.layers().indexOf(layer), dyeColor);

            if (color != 0) {
                this.kilt$wrappedHumanoidModel.getWrapped().set(modelRef.get());
                var texture = ClientHooks.getArmorTexture(entity, stack, layer, flag, slot);
                original.call(instance, poseStack, bufferSource, packedLight, this.kilt$wrappedHumanoidModel, color, texture);
            }
        } else {
            original.call(instance, poseStack, bufferSource, packedLight, humanoidModel, dyeColor, resourceLocation);
        }
    }

    @WrapOperation(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/HumanoidModel;Z)V"))
    private void kilt$tryUseWrapperForTrim(HumanoidArmorLayer<T, M, A> instance, Holder<ArmorMaterial> holder, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ArmorTrim armorTrim, A humanoidModel, boolean bl, Operation<Void> original, @Share(value = "model", namespace = Kilt.MOD_ID) LocalRef<Model> modelRef, @Share(value = "extensions", namespace = Kilt.MOD_ID) LocalRef<IClientItemExtensions> extensionsRef) {
        if (extensionsRef.get() != IClientItemExtensions.DEFAULT || KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), HumanoidArmorLayer.class, "getArmorModelHook", LivingEntity.class, ItemStack.class, EquipmentSlot.class, HumanoidModel.class)) {
            this.kilt$wrappedHumanoidModel.getWrapped().set(modelRef.get());
            original.call(instance, holder, poseStack, multiBufferSource, i, armorTrim, this.kilt$wrappedHumanoidModel, bl);
        } else {
            original.call(instance, holder, poseStack, multiBufferSource, i, armorTrim, humanoidModel, bl);
        }
    }

    @WrapOperation(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderGlint(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/HumanoidModel;)V"))
    private void kilt$tryUseWrapperForGlint(HumanoidArmorLayer<T, M, A> instance, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, A humanoidModel, Operation<Void> original, @Share(value = "model", namespace = Kilt.MOD_ID) LocalRef<Model> modelRef, @Share(value = "extensions", namespace = Kilt.MOD_ID) LocalRef<IClientItemExtensions> extensionsRef) {
        if (extensionsRef.get() != IClientItemExtensions.DEFAULT || KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), HumanoidArmorLayer.class, "getArmorModelHook", LivingEntity.class, ItemStack.class, EquipmentSlot.class, HumanoidModel.class)) {
            this.kilt$wrappedHumanoidModel.getWrapped().set(modelRef.get());
            original.call(instance, poseStack, multiBufferSource, i, this.kilt$wrappedHumanoidModel);
        } else {
            original.call(instance, poseStack, multiBufferSource, i, humanoidModel);
        }
    }

    protected Model getArmorModelHook(T entity, ItemStack stack, EquipmentSlot slot, A model) {
        return ClientHooks.getArmorModel(entity, stack, slot, model);
    }

    // Kilt: let's just. copy these. Mowzie's Mobs needs these.
    private void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Model model, int dyeColor, ResourceLocation textureLocation) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(textureLocation));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, dyeColor);
    }

    private void renderTrim(Holder<ArmorMaterial> armorMaterial, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ArmorTrim trim, Model model, boolean innerTexture) {
        TextureAtlasSprite textureAtlasSprite = this.armorTrimAtlas.getSprite(innerTexture ? trim.innerTexture(armorMaterial) : trim.outerTexture(armorMaterial));
        VertexConsumer vertexConsumer = textureAtlasSprite.wrap(bufferSource.getBuffer(Sheets.armorTrimsSheet(trim.pattern().value().decal())));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
    }

    private void renderGlint(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Model model) {
        model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.armorEntityGlint()), packedLight, OverlayTexture.NO_OVERLAY);
    }
}
