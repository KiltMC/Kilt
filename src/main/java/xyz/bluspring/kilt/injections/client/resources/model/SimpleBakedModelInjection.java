package xyz.bluspring.kilt.injections.client.resources.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraftforge.client.RenderTypeGroup;

import java.util.List;
import java.util.Map;

public interface SimpleBakedModelInjection {
    static SimpleBakedModel create(List<BakedQuad> list, Map<Direction, List<BakedQuad>> map, boolean bl, boolean bl2, boolean bl3, TextureAtlasSprite textureAtlasSprite, ItemTransforms itemTransforms, ItemOverrides itemOverrides, RenderTypeGroup renderTypeGroup) {
        var model = new SimpleBakedModel(list, map, bl, bl2, bl3, textureAtlasSprite, itemTransforms, itemOverrides);
        ((SimpleBakedModelInjection) model).addRenderTypes(renderTypeGroup);

        return model;
    }

    default void addRenderTypes(RenderTypeGroup renderTypeGroup) {
        throw new IllegalStateException();
    }
}
