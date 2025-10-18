package xyz.bluspring.kilt.injections.client.resources.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.RenderTypeGroup;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

import java.util.List;
import java.util.Map;

@FabricInjectedInterface(SimpleBakedModel.class)
public interface SimpleBakedModelInjection {
    static SimpleBakedModel create(List<BakedQuad> unculledFaces, Map<Direction, List<BakedQuad>> culledFaces, boolean hasAmbientOcclusion, boolean usesBlockLight, boolean isGui3d, TextureAtlasSprite particleIcon, ItemTransforms transforms, ItemOverrides overrides, RenderTypeGroup renderTypeGroup) {
        var model = new SimpleBakedModel(unculledFaces, culledFaces, hasAmbientOcclusion, usesBlockLight, isGui3d, particleIcon, transforms, overrides);
        model.kilt$addRenderTypes(renderTypeGroup);

        return model;
    }

    default void kilt$addRenderTypes(RenderTypeGroup renderTypeGroup) {
        throw new IllegalStateException();
    }
}
