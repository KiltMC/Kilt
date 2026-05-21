// TRACKED HASH: decab18fc678570fcc5f2c2b899561ef7d4dcd7a
package xyz.bluspring.kilt.injects.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.fml.common.asm.enumextension.ReservedConstructor;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.model.HumanoidModelArmPoseInjection;

@Mixin(HumanoidModel.class)
public class HumanoidModelInject {
    // TODO: oh boy

    @Mixin(HumanoidModel.ArmPose.class)
    public static class ArmPoseInject implements HumanoidModelArmPoseInjection, IExtensibleEnum {

        @ReservedConstructor
        private ArmPoseInject(String name, int ordinal, final boolean twoHanded) {}

        @CreateInitializer
        private ArmPoseInject(String name, int ordinal, final boolean twoHanded, IArmPoseTransformer forgeArmPose) {
            this(name, ordinal, twoHanded);
            this.forgeArmPose = forgeArmPose;
        }

        private IArmPoseTransformer forgeArmPose;

        @Override
        public <T extends LivingEntity> void applyTransform(HumanoidModel<T> model, T entity, HumanoidArm arm) {
            if (forgeArmPose != null)
                forgeArmPose.applyTransform(model, entity, arm);
        }

        @CreateStatic
        private static ExtensionInfo getExtensionInfo() {
            return ExtensionInfo.nonExtended(HumanoidModel.ArmPose.class);
        }
    }
}