package xyz.bluspring.kilt.injections.client.resources.model.geometry;

import com.mojang.blaze3d.platform.Transparency;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3fc;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

public interface BakedQuadInjection {
    default BakedNormals bakedNormals() {
        throw KiltHelper.createMixinException(BakedQuadInjection.class, "bakedNormals");
    }

    default BakedColors bakedColors() {
        throw KiltHelper.createMixinException(BakedQuadInjection.class, "bakedColors");
    }

    static BakedQuad create(Vector3fc position0, Vector3fc position1, Vector3fc position2, Vector3fc position3, long packedUV0, long packedUV1, long packedUV2, long packedUV3, Direction direction, BakedQuad.MaterialInfo materialInfo, BakedNormals bakedNormals, BakedColors bakedColors) {
        var quad = new BakedQuad(position0, position1, position2, position3, packedUV0, packedUV1, packedUV2, packedUV3, direction, materialInfo);
        ClientRecordMixinRefMaps.EXTENDED_BAKED_QUAD.put(quad, new ClientRecordMixinRefMaps.ExtendedBakedQuad(bakedNormals, bakedColors));
        return quad;
    }

    interface MaterialInfoInjection {
        default boolean ambientOcclusion() {
            throw KiltHelper.createMixinException(BakedQuadInjection.class, "ambientOcclusion");
        }

        static BakedQuad.MaterialInfo create(TextureAtlasSprite sprite, ChunkSectionLayer layer, RenderType itemRenderType, int tintIndex, boolean shade, int lightEmission, boolean ambientOcclusion) {
            var info = new BakedQuad.MaterialInfo(sprite, layer, itemRenderType, tintIndex, shade, lightEmission);
            ClientRecordMixinRefMaps.EXTENDED_BAKED_QUAD_MATERIAL_INFO.put(info, new ClientRecordMixinRefMaps.ExtendedBakedQuadMaterialInfo(ambientOcclusion));
            return info;
        }

        static BakedQuad.MaterialInfo of(Material.Baked material, Transparency transparency, int tintIndex, boolean shade, int lightEmission, boolean ambientOcclusion) {
            var info = BakedQuad.MaterialInfo.of(material, transparency, tintIndex, shade, lightEmission);
            ClientRecordMixinRefMaps.EXTENDED_BAKED_QUAD_MATERIAL_INFO.put(info, new ClientRecordMixinRefMaps.ExtendedBakedQuadMaterialInfo(ambientOcclusion));
            return info;
        }
    }
}
