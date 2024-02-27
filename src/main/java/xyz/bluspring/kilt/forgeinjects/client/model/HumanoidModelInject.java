// TRACKED HASH: decab18fc678570fcc5f2c2b899561ef7d4dcd7a
package xyz.bluspring.kilt.forgeinjects.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.IArmPoseTransformer;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.model.HumanoidModelArmPoseInjection;

@Mixin(HumanoidModel.class)
public class HumanoidModelInject {

    @Mixin(HumanoidModel.ArmPose.class)
    public static class ArmPoseInject implements HumanoidModelArmPoseInjection, IExtensibleEnum {
        @CreateStatic
        private static HumanoidModel.ArmPose create(String name, boolean twoHanded, IArmPoseTransformer forgeArmPose) {
            return HumanoidModelArmPoseInjection.create(name, twoHanded, forgeArmPose);
        }

        private IArmPoseTransformer forgeArmPose;

        @Override
        public <T extends LivingEntity> void applyTransform(HumanoidModel<T> model, T entity, HumanoidArm arm) {
            if (forgeArmPose != null)
                forgeArmPose.applyTransform(model, entity, arm);
        }

        @Override
        public void setArmPose(IArmPoseTransformer forgeArmPose) {
            this.forgeArmPose = forgeArmPose;
        }
    }
}