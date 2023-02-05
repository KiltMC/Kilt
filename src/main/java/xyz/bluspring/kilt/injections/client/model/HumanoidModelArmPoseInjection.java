package xyz.bluspring.kilt.injections.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.IArmPoseTransformer;
import xyz.bluspring.kilt.mixin.ArmPoseAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

public interface HumanoidModelArmPoseInjection {
    static HumanoidModel.ArmPose create(String name, boolean twoHanded, IArmPoseTransformer forgeArmPose) {
        var value = EnumUtils.addEnumToClass(
                HumanoidModel.ArmPose.class, ArmPoseAccessor.getValues(),
                name, (size) -> ArmPoseAccessor.createArmPose(name, size, twoHanded),
                (values) -> ArmPoseAccessor.setValues(values.toArray(new HumanoidModel.ArmPose[0]))
        );

        ((HumanoidModelArmPoseInjection) (Object) value).setArmPose(forgeArmPose);

        return value;
    }

    default <T extends LivingEntity> void applyTransform(HumanoidModel<T> model, T entity, HumanoidArm arm) {
        throw new IllegalStateException();
    }

    default void setArmPose(IArmPoseTransformer forgeArmPose) {
        throw new IllegalStateException();
    }
}
