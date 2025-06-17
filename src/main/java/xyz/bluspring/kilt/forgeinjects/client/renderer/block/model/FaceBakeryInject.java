package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ForgeFaceData;
import net.minecraftforge.client.model.QuadTransformers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BakedQuadInjection;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BlockElementFaceInjection;

@Mixin(FaceBakery.class)
public abstract class FaceBakeryInject {
    @WrapOperation(method = "bakeQuad", at = @At(value = "NEW", target = "([IILnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Z)Lnet/minecraft/client/renderer/block/model/BakedQuad;"))
    private BakedQuad kilt$transformQuadFromForge(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, Operation<BakedQuad> original, @Local(argsOnly = true) BlockElementFace face) {
        ForgeHooksClient.fillNormal(vertices, direction, ((BlockElementFaceInjection) face).getFaceData().calculateNormals());
        var data = ((BlockElementFaceInjection) face).getFaceData();

        var quad = data.ambientOcclusion() ? original.call(vertices, tintIndex, direction, sprite, shade) : BakedQuadInjection.withAo(vertices, tintIndex, direction, sprite, shade, false);
        if (!ForgeFaceData.DEFAULT.equals(data)) {
            Renderer renderer = RendererAccess.INSTANCE.getRenderer();
            QuadEmitter emitter = renderer.meshBuilder().getEmitter().fromVanilla(original.call(vertices, tintIndex, direction, sprite, shade), renderer.materialFinder().ambientOcclusion(TriState.of(data.ambientOcclusion())).find(), direction);
            int light = LightTexture.pack(data.blockLight(), data.skyLight());
            emitter.lightmap(light, light, light, light);
            emitter.color(data.color(), data.color(), data.color(), data.color());
            return emitter.toBakedQuad(sprite);
//            QuadTransformers.applyingLightmap(data.blockLight(), data.skyLight()).processInPlace(quad);
//            QuadTransformers.applyingColor(data.color()).processInPlace(quad);
        }

        return quad;
    }

    // Kilt: the UV patch may target fixing item texture seams? we don't really want to fix those ourselves, so.
}
