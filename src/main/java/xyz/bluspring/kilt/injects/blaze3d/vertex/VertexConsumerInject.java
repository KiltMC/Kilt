package xyz.bluspring.kilt.injects.blaze3d.vertex;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.neoforge.client.extensions.IVertexConsumerExtension;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.ARGB;

@Mixin(VertexConsumer.class)
public interface VertexConsumerInject extends IVertexConsumerExtension {
    @WrapOperation(method = {"putBakedQuad", "putBlockBakedQuad"}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/QuadInstance;getColor(I)I"))
    private int kilt$applyBakedColorFromQuad(QuadInstance instance, int vertex, Operation<Integer> original, @Local(argsOnly = true, name = "quad") BakedQuad quad) {
        return ARGB.multiply(original.call(instance, vertex), quad.bakedColors().color(vertex));
    }

    @Inject(method = "putBakedQuad", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(FFFIFFIIFFF)V"))
    private void kilt$applyBakedNormalsFromQuad(PoseStack.Pose pose, BakedQuad quad, QuadInstance instance, CallbackInfo ci, @Local(name = "vertex") int vertex, @Local(name = "normal") Vector3f normal) {
        applyBakedNormals(normal, quad.bakedNormals(), vertex, pose.normal());
    }
}
