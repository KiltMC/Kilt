// TRACKED HASH: 16d8228163cb34e67ad0a852b03a7ae5d7a2e843
package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraftforge.common.util.TransformationHelper;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.renderer.block.model.ItemTransformInjection;

@Mixin(ItemTransform.class)
public abstract class ItemTransformInject implements ItemTransformInjection {
    // TODO: how do we make this link with Porting Lib?
    public Vector3f rightRotation = new Vector3f();

    public ItemTransformInject(Vector3f leftRotation, Vector3f translation, Vector3f scale) {}

    @CreateInitializer
    public ItemTransformInject(Vector3f leftRotation, Vector3f translation, Vector3f scale, Vector3f rightRotation) {
        this(leftRotation, translation, scale);
        this.rightRotation = rightRotation;
    }

    @Inject(method = "apply", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V", shift = At.Shift.AFTER))
    private void kilt$applyRightRotation(boolean leftHand, PoseStack poseStack, CallbackInfo ci) {
        poseStack.mulPose(TransformationHelper.quatFromXYZ(rightRotation.x(), rightRotation.y() * (leftHand ? -1 : 1), rightRotation.z() * (leftHand ? -1 : 1), true));
    }
}