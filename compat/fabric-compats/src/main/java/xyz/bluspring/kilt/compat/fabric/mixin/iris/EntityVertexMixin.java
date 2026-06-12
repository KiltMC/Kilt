package xyz.bluspring.kilt.compat.fabric.mixin.iris;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.irisshaders.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertex;
import org.joml.Matrix3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.compat.fabric.sodium.ModelQuadViewExtension;
import xyz.bluspring.kilt.compat.fabric.sodium.ModelQuadUtilExtension;

@IfModLoaded("iris")
@Mixin(value = EntityVertex.class, remap = false)
public abstract class EntityVertexMixin {

    // Copied from: https://github.com/Asek3/Oculus/blob/1.20.1-new/src/sodiumCompatibility/java/net/irisshaders/iris/compat/sodium/impl/vertex_format/entity_xhfp/EntityVertex.java#L91-L104
    @Unique
    private static int kilt$iris$multARGBInts(int colorA, int colorB) {
        // Most common case: Either quad coloring or tint-based coloring, but not both
        if (colorA == -1) {
            return colorB;
        } else if (colorB == -1) {
            return colorA;
        }
        // General case (rare): Both colorings, actually perform the multiplication
        int a = (int) ((ColorARGB.unpackAlpha(colorA) / 255.0f) * (ColorARGB.unpackAlpha(colorB) / 255.0f) * 255.0f);
        int b = (int) ((ColorARGB.unpackBlue(colorA) / 255.0f) * (ColorARGB.unpackBlue(colorB) / 255.0f) * 255.0f);
        int g = (int) ((ColorARGB.unpackGreen(colorA) / 255.0f) * (ColorARGB.unpackGreen(colorB) / 255.0f) * 255.0f);
        int r = (int) ((ColorARGB.unpackRed(colorA) / 255.0f) * (ColorARGB.unpackRed(colorB) / 255.0f) * 255.0f);
        return ColorARGB.pack(r, g, b, a);
    }

    @ModifyArg(
        method = "writeQuadVertices",
        at = @At(
            value = "INVOKE",
            target = "Lnet/irisshaders/iris/compat/sodium/impl/vertex_format/entity_xhfp/EntityVertex;write2(JFFFIFFFFIII)V"
        ),
        index = 4
    )
    private static int kilt$writeQuadVertices$multARGBInts(
        int color, @Local(argsOnly = true) ModelQuadView quad,
        @Local(ordinal = 4) int i
    ) {
        return kilt$iris$multARGBInts(quad.getColor(i), color);
    }

    @ModifyArg(
        method = "writeQuadVertices",
        at = @At(
            value = "INVOKE",
            target = "Lnet/irisshaders/iris/compat/sodium/impl/vertex_format/entity_xhfp/EntityVertex;write2(JFFFIFFFFIII)V"
        ),
        index = 9
    )
    private static int kilt$writeQuadVertices$mergeBakedLight(
        int light, @Local(argsOnly = true) ModelQuadView quad,
        @Local(ordinal = 4) int i
    ) {
        return ModelQuadUtilExtension.mergeBakedLight(((ModelQuadViewExtension) quad).getLight(i), light);
    }

    @ModifyArg(
        method = "writeQuadVertices",
        at = @At(
            value = "INVOKE",
            target = "Lnet/irisshaders/iris/compat/sodium/impl/vertex_format/entity_xhfp/EntityVertex;write2(JFFFIFFFFIII)V"
        ),
        index = 11
    )
    private static int kilt$writeQuadVertices$getForgeNormal(
        int nt,
        @Local(argsOnly = true) ModelQuadView quad,
        @Local(ordinal = 4) int i,
        @Local Matrix3f matNormal,
        @Local(ordinal = 5) LocalFloatRef nxt,
        @Local(ordinal = 6) LocalFloatRef nyt,
        @Local(ordinal = 7) LocalFloatRef nzt
    ) {
        int n = ((ModelQuadViewExtension) quad).getForgeNormal(i);
        if ((n & 0xFFFFFF) != 0) {
            float nx = NormI8.unpackX(n);
            float ny = NormI8.unpackY(n);
            float nz = NormI8.unpackZ(n);
            nxt.set(MatrixHelper.transformNormalX(matNormal, nx, ny, nz));
            nyt.set(MatrixHelper.transformNormalY(matNormal, nx, ny, nz));
            nzt.set(MatrixHelper.transformNormalZ(matNormal, nx, ny, nz));
            return NormI8.pack(nxt.get(), nyt.get(), nzt.get());
        }
        return nt;
    }

}
