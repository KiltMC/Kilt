// TRACKED HASH: 6e5ff0663e40cf957dd3b217b0541c32bd378ce0
package xyz.bluspring.kilt.injects.blaze3d.vertex;

import java.nio.ByteBuffer;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.neoforge.client.extensions.IVertexConsumerExtension;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.block.model.BakedQuad;

@Mixin(VertexConsumer.class)
public interface VertexConsumerInject extends IVertexConsumerExtension {
    @ModifyArg(method = "putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFFF[IIZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/FastColor$ARGB32;color(IIII)I"), index = 0)
    private int kilt$applyQuadAlpha(int originalAlpha, @Local(argsOnly = true) boolean readAlpha, @Local(argsOnly = true, ordinal = 3) float alpha, @Local ByteBuffer buffer) {
        return readAlpha ? (int) (((alpha * (float) (buffer.get(15) & 255)) / 255f) * 255) : originalAlpha;
    }

    @Definition(id = "l", local = @Local(type = int.class, ordinal = 4))
    @Definition(id = "lightmap", local = @Local(type = int[].class, ordinal = 0, argsOnly = true))
    @Expression("lightmap[l]")
    @ModifyExpressionValue(method = "putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFFF[IIZ)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int kilt$applyBakedLightingToBuffer(int original, @Local ByteBuffer buffer) {
        return applyBakedLighting(original, buffer);
    }

    @Inject(method = "putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFFF[IIZ)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(FFFIFFIIFFF)V"))
    private void kilt$applyBakedNormals(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay, boolean readAlpha, CallbackInfo ci, @Local ByteBuffer buffer, @Local(ordinal = 0) Vector3f normal) {
        applyBakedNormals(normal, buffer, pose.normal());
    }
}