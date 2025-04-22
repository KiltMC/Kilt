package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraftforge.common.util.TransformationHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.render.block.model.ItemTransformInjection;

@Mixin(ItemTransform.class)
public abstract class ItemTransformInject implements ItemTransformInjection {
    public Vector3f rightRotation = Vector3f.ZERO.copy();

    @Override
    public Vector3f getRightRotation() {
        return rightRotation;
    }

    @Override
    public void setRightRotation(Vector3f rightRotation) {
        this.rightRotation = rightRotation.copy();
    }

    public ItemTransformInject(Vector3f leftRotation, Vector3f translation, Vector3f scale) {}

    @CreateInitializer
    public ItemTransformInject(Vector3f leftRotation, Vector3f translation, Vector3f scale, Vector3f rightRotation) {
        this(leftRotation, translation, scale);

        this.setRightRotation(rightRotation);
    }

    @Inject(method = "apply", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V", shift = At.Shift.AFTER))
    private void kilt$applyRightRotation(boolean leftHand, PoseStack poseStack, CallbackInfo ci) {
        poseStack.mulPose(TransformationHelper.quatFromXYZ(new Vector3f(rightRotation.x(), rightRotation.y() * (leftHand ? -1 : 1), rightRotation.z() * (leftHand ? -1 : 1)), true));
    }
}
