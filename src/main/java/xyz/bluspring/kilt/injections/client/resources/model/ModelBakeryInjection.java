package xyz.bluspring.kilt.injections.client.resources.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

import java.util.function.Function;

public interface ModelBakeryInjection {
    @FabricInjectedInterface(target = "net.minecraft.client.resources.model.ModelBakery$ModelBakerImpl")
    interface ModelBakerImplInjection {
        BakedModel bake(ResourceLocation loc, ModelState state, Function<Material, TextureAtlasSprite> sprites);

        Function<Material, TextureAtlasSprite> getModelTextureGetter();
    }
}
