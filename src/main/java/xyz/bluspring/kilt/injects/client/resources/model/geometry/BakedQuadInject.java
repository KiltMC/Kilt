package xyz.bluspring.kilt.injects.client.resources.model.geometry;

import com.mojang.blaze3d.platform.Transparency;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.resources.model.geometry.BakedQuadInjection;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

@Mixin(BakedQuad.class)
public abstract class BakedQuadInject implements BakedQuadInjection {
    public BakedQuadInject(Vector3fc position0, Vector3fc position1, Vector3fc position2, Vector3fc position3, long packedUV0, long packedUV1, long packedUV2, long packedUV3, Direction direction, BakedQuad.MaterialInfo materialInfo) {}

    @CreateInitializer
    public BakedQuadInject(Vector3fc position0, Vector3fc position1, Vector3fc position2, Vector3fc position3, long packedUV0, long packedUV1, long packedUV2, long packedUV3, Direction direction, BakedQuad.MaterialInfo materialInfo, BakedNormals bakedNormals, BakedColors bakedColors) {
        this(position0, position1, position2, position3, packedUV0, packedUV1, packedUV2, packedUV3, direction, materialInfo);
        ClientRecordMixinRefMaps.EXTENDED_BAKED_QUAD.put((BakedQuad) (Object) this, new ClientRecordMixinRefMaps.ExtendedBakedQuad(bakedNormals, bakedColors));
    }

    @Override
    public BakedNormals bakedNormals() {
        var extension = ClientRecordMixinRefMaps.EXTENDED_BAKED_QUAD.get(this);
        if (extension != null)
            return extension.bakedNormals();

        return BakedNormals.UNSPECIFIED;
    }

    @Override
    public BakedColors bakedColors() {
        var extension = ClientRecordMixinRefMaps.EXTENDED_BAKED_QUAD.get(this);
        if (extension != null)
            return extension.bakedColors();

        return BakedColors.DEFAULT;
    }

    @CreateStatic
    private static BakedQuad create(Vector3fc position0, Vector3fc position1, Vector3fc position2, Vector3fc position3, long packedUV0, long packedUV1, long packedUV2, long packedUV3, Direction direction, BakedQuad.MaterialInfo materialInfo, BakedNormals bakedNormals, BakedColors bakedColors) {
        return BakedQuadInjection.create(position0, position1, position2, position3, packedUV0, packedUV1, packedUV2, packedUV3, direction, materialInfo, bakedNormals, bakedColors);
    }

    @Mixin(BakedQuad.MaterialInfo.class)
    public abstract static class MaterialInfoInject implements MaterialInfoInjection {
        public MaterialInfoInject(TextureAtlasSprite sprite, ChunkSectionLayer layer, RenderType itemRenderType, int tintIndex, boolean shade, int lightEmission) {}

        @CreateInitializer
        public MaterialInfoInject(TextureAtlasSprite sprite, ChunkSectionLayer layer, RenderType itemRenderType, int tintIndex, boolean shade, int lightEmission, boolean ambientOcclusion) {
            this(sprite, layer, itemRenderType, tintIndex, shade, lightEmission);
            ClientRecordMixinRefMaps.EXTENDED_BAKED_QUAD_MATERIAL_INFO.put((BakedQuad.MaterialInfo) (Object) this, new ClientRecordMixinRefMaps.ExtendedBakedQuadMaterialInfo(ambientOcclusion));
        }

        @Override
        public boolean ambientOcclusion() {
            var extension = ClientRecordMixinRefMaps.EXTENDED_BAKED_QUAD_MATERIAL_INFO.get(this);
            if (extension != null)
                return extension.ambientOcclusion();

            return true;
        }

        @CreateStatic
        private static BakedQuad.MaterialInfo of(Material.Baked material, Transparency transparency, int tintIndex, boolean shade, int lightEmission, boolean ambientOcclusion) {
            return BakedQuadInjection.MaterialInfoInjection.of(material, transparency, tintIndex, shade, lightEmission, ambientOcclusion);
        }
    }
}
