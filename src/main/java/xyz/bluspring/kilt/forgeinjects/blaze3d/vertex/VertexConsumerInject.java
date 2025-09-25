// TRACKED HASH: 6e5ff0663e40cf957dd3b217b0541c32bd378ce0
package xyz.bluspring.kilt.forgeinjects.blaze3d.vertex;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.extensions.IForgeVertexConsumer;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.bluspring.kilt.injections.blaze3d.vertex.VertexConsumerInjection;

import java.nio.ByteBuffer;

@Mixin(VertexConsumer.class)
public interface VertexConsumerInject extends VertexConsumerInjection, IForgeVertexConsumer {
    @Shadow void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float[] fs, float f, float g, float h, int[] is, int i, boolean bl);

    @Override
    default void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float[] fs, float f, float g, float h, float alpha, int[] is, int i, boolean bl) {
        VertexConsumerInjection.alpha.set(alpha);
        putBulkData(pose, bakedQuad, fs, f, g, h, is, i, bl);
        VertexConsumerInjection.alpha.reset();
    }

    // Most certainly going to break in 1.21.1 so beware
    @ModifyVariable(method = "putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFF[IIZ)V", at = @At("STORE"), ordinal = 4)
    private int kilt$applyLightmap(int original, @Local(ordinal = 0) ByteBuffer byteBuffer) {
        return applyBakedLighting(original, byteBuffer);
    }

    @ModifyExpressionValue(method = "putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFF[IIZ)V", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
    private float kilt$useVertexAlpha(float original, @Local(argsOnly = true) PoseStack.Pose pose, @Local(argsOnly = true) boolean multiColor, @Local(ordinal = 0) Vector3f vector3f, @Local(ordinal = 0) ByteBuffer byteBuffer) {
        applyBakedNormals(vector3f, byteBuffer, pose.normal());
        return multiColor ? VertexConsumerInjection.alpha.getOrElse(original) * (float) (byteBuffer.get(15) & 255) / 255F : VertexConsumerInjection.alpha.getOrElse(original);
    }
}