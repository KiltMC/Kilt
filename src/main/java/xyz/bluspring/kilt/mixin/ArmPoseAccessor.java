package xyz.bluspring.kilt.mixin;

import net.minecraft.client.model.HumanoidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HumanoidModel.ArmPose.class)
public interface ArmPoseAccessor {
    @Invoker("<init>")
    static HumanoidModel.ArmPose createArmPose(String name, int id, boolean bl) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({"MixinAnnotationTarget"})
    @Accessor("$VALUES")
    static HumanoidModel.ArmPose[] getValues() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    @Mutable
    static void setValues(HumanoidModel.ArmPose[] values) {
        throw new IllegalStateException();
    }
}
