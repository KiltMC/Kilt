package xyz.bluspring.kilt.injections.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public interface HumanoidArmorLayerInjection<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    default void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, T livingEntity, EquipmentSlot slot, int packedLight, A model, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        throw KiltHelper.createMixinException(HumanoidArmorLayerInjection.class, "renderArmorPiece");
    }
}
