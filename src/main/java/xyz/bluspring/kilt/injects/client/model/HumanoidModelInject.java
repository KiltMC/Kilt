// TRACKED HASH: decab18fc678570fcc5f2c2b899561ef7d4dcd7a
package xyz.bluspring.kilt.injects.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.model.HumanoidModelArmPoseInjection;

@Mixin(HumanoidModel.class)
public class HumanoidModelInject {
    // TODO: oh boy

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

        @CreateStatic
        private static ExtensionInfo getExtensionInfo() {
            return ExtensionInfo.nonExtended((Class) HumanoidModel.ArmPose.class);
        }
    }
}