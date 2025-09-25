package xyz.bluspring.kilt.injections.blaze3d.vertex;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;
import xyz.bluspring.kilt.util.FloatReference;

import java.util.concurrent.atomic.AtomicReference;

public interface VertexConsumerInjection {
    FloatReference alpha = new FloatReference();

    default void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float[] fs, float f, float g, float h, float alpha, int[] is, int i, boolean bl) {
        throw new IllegalStateException();
    }
}
