package xyz.bluspring.kilt.injections.client.resources.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.RenderTypeGroup;

import java.util.List;
import java.util.Map;

public interface SimpleBakedModelInjection {
    static SimpleBakedModel create(List<BakedQuad> list, Map<Direction, List<BakedQuad>> map, boolean bl, boolean bl2, boolean bl3, TextureAtlasSprite textureAtlasSprite, ItemTransforms itemTransforms, ItemOverrides itemOverrides, RenderTypeGroup renderTypeGroup, RenderTypeGroup fastRenderTypeGroup) {
        var model = new SimpleBakedModel(list, map, bl, bl2, bl3, textureAtlasSprite, itemTransforms, itemOverrides);
        ((SimpleBakedModelInjection) model).kilt$addRenderTypes(renderTypeGroup);
        ((SimpleBakedModelInjection) model).kilt$addRenderTypesFast(fastRenderTypeGroup);

        return model;
    }

    static SimpleBakedModel create(List<BakedQuad> list, Map<Direction, List<BakedQuad>> map, boolean bl, boolean bl2, boolean bl3, TextureAtlasSprite textureAtlasSprite, ItemTransforms itemTransforms, ItemOverrides itemOverrides, RenderTypeGroup renderTypeGroup) {
        return create(list, map, bl, bl2, bl3, textureAtlasSprite, itemTransforms, itemOverrides, renderTypeGroup, renderTypeGroup);
    }

    default void kilt$addRenderTypes(RenderTypeGroup renderTypeGroup) {
        throw new IllegalStateException();
    }

    default void kilt$addRenderTypesFast(RenderTypeGroup renderTypeGroup) {
        throw new IllegalStateException();
    }
}
