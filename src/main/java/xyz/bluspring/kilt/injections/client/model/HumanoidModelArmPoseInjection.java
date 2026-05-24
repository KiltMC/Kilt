package xyz.bluspring.kilt.injections.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public interface HumanoidModelArmPoseInjection {

    default <T extends LivingEntity> void applyTransform(HumanoidModel<T> model, T entity, HumanoidArm arm) {
        throw new IllegalStateException();
    }
}
