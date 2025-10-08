package xyz.bluspring.kilt.mixin.compat.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.render.immediate.model.BakedModelEncoder;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.helpers.SodiumRenderStorage;

@IfModLoaded("sodium")
@Mixin(BakedModelEncoder.class)
public class BakedModelEncoderMixin {
    @ModifyArg(
            method = "writeQuadVertices(Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lme/jellysquid/mods/sodium/client/model/quad/ModelQuadView;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/api/vertex/format/common/ModelVertex;write(JFFFIFFIII)V"
            ),
            index = 4
    )
    private static int multiplyQuadColor(int color, @Local(argsOnly = true) ModelQuadView quad, @Local(ordinal = 4) int i) {
        if (SodiumRenderStorage.ITEM_RENDERING)
            return kilt$mulComponentWise(color, quad.getColor(i));
        return color;
    }

    // Taken from sodium 0.6 1.21.1
    /**
     * <p>Multiplies the packed 8-bit values component-wise to produce 16-bit intermediaries, and then round to the
     * nearest 8-bit representation (similar to floating-point.)</p>
     *
     * @param color0 The first color to multiply
     * @param color1 The second color to multiply
     * @return The product of the two colors
     */
    private static int kilt$mulComponentWise(int color0, int color1) {
        int comp0 = ((((color0 >>>  0) & 0xFF) * ((color1 >>>  0) & 0xFF)) + 0xFF) >>> 8;
        int comp1 = ((((color0 >>>  8) & 0xFF) * ((color1 >>>  8) & 0xFF)) + 0xFF) >>> 8;
        int comp2 = ((((color0 >>> 16) & 0xFF) * ((color1 >>> 16) & 0xFF)) + 0xFF) >>> 8;
        int comp3 = ((((color0 >>> 24) & 0xFF) * ((color1 >>> 24) & 0xFF)) + 0xFF) >>> 8;

        return (comp0 << 0) | (comp1 << 8) | (comp2 << 16) | (comp3 << 24);
    }
}
