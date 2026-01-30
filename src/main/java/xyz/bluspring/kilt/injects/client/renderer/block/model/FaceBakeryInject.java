package xyz.bluspring.kilt.injects.client.renderer.block.model;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.QuadTransformers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BakedQuadInjection;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BlockElementFaceInjection;

@Mixin(FaceBakery.class)
public abstract class FaceBakeryInject {
    @WrapWithCondition(method = "bakeQuad", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/FaceBakery;recalculateWinding([ILnet/minecraft/core/Direction;)V"))
    private boolean kilt$suppressWindingRecalculation(FaceBakery instance, int[] is, Direction direction, @Local(argsOnly = true) ModelState state) {
        return !state.mayApplyArbitraryRotation();
    }

    @WrapOperation(method = "bakeQuad", at = @At(value = "NEW", target = "([IILnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Z)Lnet/minecraft/client/renderer/block/model/BakedQuad;"))
    private BakedQuad kilt$transformQuadFromForge(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, Operation<BakedQuad> original, @Local(argsOnly = true) BlockElementFace face) {
        ClientHooks.fillNormal(vertices, direction);
        var data = face.faceData();

        var quad = data.ambientOcclusion() ? original.call(vertices, tintIndex, direction, sprite, shade) : BakedQuadInjection.withAo(vertices, tintIndex, direction, sprite, shade, false);
        if (!ExtraFaceData.DEFAULT.equals(data)) {
            QuadTransformers.applyingLightmap(data.blockLight(), data.skyLight()).processInPlace(quad);
            QuadTransformers.applyingColor(data.color()).processInPlace(quad);
        }

        return quad;
    }
}
