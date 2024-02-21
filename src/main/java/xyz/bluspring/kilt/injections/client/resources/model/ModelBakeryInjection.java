package xyz.bluspring.kilt.injections.client.resources.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public interface ModelBakeryInjection {
    interface ModelBakerImplInjection {
        BakedModel bake(ResourceLocation loc, ModelState state, Function<Material, TextureAtlasSprite> sprites);

        Function<Material, TextureAtlasSprite> getModelTextureGetter();
    }
}
