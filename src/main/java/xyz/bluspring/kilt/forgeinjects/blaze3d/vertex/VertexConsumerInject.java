package xyz.bluspring.kilt.forgeinjects.blaze3d.vertex;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.extensions.IForgeVertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import xyz.bluspring.kilt.injections.blaze3d.vertex.VertexConsumerInjection;

import java.nio.ByteBuffer;

@Mixin(VertexConsumer.class)
public interface VertexConsumerInject extends VertexConsumerInjection, IForgeVertexConsumer {
    @Shadow void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float[] fs, float f, float g, float h, int[] is, int i, boolean bl);

    @Override
    default void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float[] fs, float f, float g, float h, float alpha, int[] is, int i, boolean bl) {
        VertexConsumerInjection.alpha.set(alpha);
        putBulkData(pose, bakedQuad, fs, f, g, h, is, i, bl);
        VertexConsumerInjection.alpha.set(1F);
    }

    @ModifyConstant(method = "putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFF[IIZ)V", constant = @Constant(floatValue = 1.0F))
    private float kilt$useVertexAlpha(float constant, PoseStack.Pose pose, BakedQuad bakedQuad, float[] fs, float f, float g, float h, int[] is, int i, boolean bl, @Local(ordinal = 0) Vector3f vector3f, @Local(ordinal = 0) ByteBuffer byteBuffer) {
        IForgeVertexConsumer.super.applyBakedNormals(vector3f, byteBuffer, pose.normal());
        return bl ? VertexConsumerInjection.alpha.get() * (float) (byteBuffer.get(15) & 255) / 255F : VertexConsumerInjection.alpha.get();
    }
}
