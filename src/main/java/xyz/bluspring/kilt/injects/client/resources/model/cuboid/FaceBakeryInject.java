package xyz.bluspring.kilt.injects.client.resources.model.cuboid;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Transparency;
import com.mojang.math.Quadrant;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.resources.model.cuboid.FaceBakeryInjection;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

@Mixin(FaceBakery.class)
public abstract class FaceBakeryInject implements FaceBakeryInjection {
    @WrapOperation(method = "bakeQuad(Lnet/minecraft/client/resources/model/ModelBaker;Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lnet/minecraft/client/resources/model/cuboid/CuboidFace;Lnet/minecraft/client/resources/model/sprite/Material$Baked;Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/cuboid/CuboidRotation;ZI)Lnet/minecraft/client/resources/model/geometry/BakedQuad;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;of(Lnet/minecraft/client/resources/model/sprite/Material$Baked;Lcom/mojang/blaze3d/platform/Transparency;IZI)Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;"))
    private static BakedQuad.MaterialInfo kilt$passAOAndFaceLightEmission(Material.Baked material, Transparency transparency, int tintIndex, boolean shade, int lightEmission, Operation<BakedQuad.MaterialInfo> original, @Local(argsOnly = true, name = "face") CuboidFace face) {
        ExtraFaceData faceData = face.faceData();
        lightEmission = Math.max(lightEmission, faceData.lightEmission());

        var info = original.call(material, transparency, tintIndex, shade, lightEmission);
        ClientRecordMixinRefMaps.EXTENDED_BAKED_QUAD_MATERIAL_INFO.put(info, new ClientRecordMixinRefMaps.ExtendedBakedQuadMaterialInfo(faceData.ambientOcclusion()));
        return info;
    }

    @CreateStatic
    private static BakedQuad bakeQuad(final ModelBaker.Interner interner, final Vector3fc from, final Vector3fc to, final CuboidFace.UVs uvs, final Quadrant uvRotation, final BakedQuad.MaterialInfo materialInfo, final Direction facing, final ModelState modelState, final @Nullable CuboidRotation elementRotation, ExtraFaceData faceData) {
        return FaceBakeryInjection.bakeQuad(interner, from, to, uvs, uvRotation, materialInfo, facing, modelState, elementRotation, faceData);
    }

    @CreateStatic
    private static void recalculateWinding(Vector3fc[] positions, long[] uvs, Direction direction, int @Nullable [] colors, int @Nullable [] normals) {
        FaceBakeryInjection.recalculateWinding(positions, uvs, direction, colors, normals);
    }

    @WrapWithCondition(method = "bakeQuad(Lnet/minecraft/client/resources/model/ModelBaker$Interner;Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lnet/minecraft/client/resources/model/cuboid/CuboidFace$UVs;Lcom/mojang/math/Quadrant;Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/cuboid/CuboidRotation;)Lnet/minecraft/client/resources/model/geometry/BakedQuad;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/cuboid/FaceBakery;recalculateWinding([Lorg/joml/Vector3fc;[JLnet/minecraft/core/Direction;)V"))
    private static boolean kilt$suppressWindingRecalc(Vector3fc[] positions, long[] uvs, Direction direction, @Local(argsOnly = true, name = "modelState") ModelState modelState) {
        return !modelState.mayApplyArbitraryRotation();
    }

    @ModifyReturnValue(method = "bakeQuad(Lnet/minecraft/client/resources/model/ModelBaker$Interner;Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lnet/minecraft/client/resources/model/cuboid/CuboidFace$UVs;Lcom/mojang/math/Quadrant;Lnet/minecraft/client/resources/model/geometry/BakedQuad$MaterialInfo;Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/cuboid/CuboidRotation;)Lnet/minecraft/client/resources/model/geometry/BakedQuad;", at = @At("RETURN"))
    private static BakedQuad kilt$attachInternerNormalsAndColors(BakedQuad original, @Local(argsOnly = true, name = "interner") ModelBaker.Interner interner, @Local(name = "vertexPositions") Vector3fc[] vertexPositions) {
        var faceData = FaceBakeryInjection.kilt$faceData.get().top();

        ClientRecordMixinRefMaps.EXTENDED_BAKED_QUAD.put(original, new ClientRecordMixinRefMaps.ExtendedBakedQuad(
            interner.normals(BakedNormals.of(BakedNormals.computeQuadNormal(vertexPositions[0], vertexPositions[1], vertexPositions[2], vertexPositions[3]))),
            interner.colors(BakedColors.of(faceData.color()))
        ));

        return original;
    }

    @Definition(id = "swap", method = "Lnet/minecraft/client/resources/model/cuboid/FaceBakery;swap([JII)V")
    @Definition(id = "uvs", local = @Local(type = long[].class, name = "uvs", argsOnly = true))
    @Expression("swap(uvs, ?, ?)")
    @Inject(method = "recalculateWinding", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private static void kilt$trySwapColorsAndNormals(Vector3fc[] positions, long[] uvs, Direction direction, CallbackInfo ci, @Local(name = "vertexToSwap") int vertexToSwap, @Local(name = "vertex") int vertex) {
        var colors = FaceBakeryInjection.kilt$colors.get();
        var normals = FaceBakeryInjection.kilt$normals.get();

        if (colors != null) {
            swap(colors, vertexToSwap, vertex);
        }

        if (normals != null) {
            swap(normals, vertexToSwap, vertex);
        }
    }

    @Unique
    private static void swap(int[] array, int indexA, int indexB) {
        int tmp = array[indexA];
        array[indexA] = array[indexB];
        array[indexB] = tmp;
    }
}
