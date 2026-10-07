package xyz.bluspring.kilt.mixin.compat.iris;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.irisshaders.iris.helpers.EntityState;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;

// directly copied from https://github.com/IrisShaders/Iris/blob/1.21.1/neoforge/src/main/java/net/irisshaders/iris/mixin/forge/MixinHumanoidArmorLayer.java
@IfModLoaded("iris")
@Mixin(value = HumanoidArmorLayer.class, priority = 1010)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @TargetHandler(mixin = "xyz.bluspring.kilt.injects.client.renderer.entity.layers.HumanoidArmorLayerInject", name = "renderTrim")
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "HEAD"))
    private void changeTrimTempForge(Holder<ArmorMaterial> holder, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ArmorTrim armorTrim, Model humanoidModel, boolean bl, CallbackInfo ci) {
        if (WorldRenderingSettings.INSTANCE.getItemIds() == null) return;

        EntityState.interposeItemId(WorldRenderingSettings.INSTANCE.getItemIds().applyAsInt(new NamespacedId("minecraft", "trim_" + armorTrim.material().value().assetName())));
    }

    @TargetHandler(mixin = "xyz.bluspring.kilt.injects.client.renderer.entity.layers.HumanoidArmorLayerInject", name = "renderArmorPiece")
    @Inject(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;copyPropertiesTo(Lnet/minecraft/client/model/HumanoidModel;)V"), require = 0)
    private void changeIdF(PoseStack poseStack, MultiBufferSource bufferSource, T livingEntity, EquipmentSlot slot, int packedLight, A model, CallbackInfo ci, @Local ArmorItem lvArmorItem8) {
        if (WorldRenderingSettings.INSTANCE.getItemIds() == null) return;

        ResourceLocation location = BuiltInRegistries.ITEM.getKey(lvArmorItem8);
        CapturedRenderingState.INSTANCE.setCurrentRenderedItem(WorldRenderingSettings.INSTANCE.getItemIds().applyAsInt(new NamespacedId(location.getNamespace(), location.getPath())));
    }

    @TargetHandler(mixin = "xyz.bluspring.kilt.injects.client.renderer.entity.layers.HumanoidArmorLayerInject", name = "renderTrim")
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "TAIL"))
    private void changeTrimTemp2Forge(Holder<ArmorMaterial> holder, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ArmorTrim armorTrim, Model humanoidModel, boolean bl, CallbackInfo ci) {
        EntityState.restoreItemId();
    }
}
