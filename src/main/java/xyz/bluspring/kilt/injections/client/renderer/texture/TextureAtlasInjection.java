package xyz.bluspring.kilt.injections.client.renderer.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Map;
import java.util.Set;

public interface TextureAtlasInjection {
    default Map<ResourceLocation, TextureAtlasSprite> getTextures() {
        throw KiltHelper.createMixinException(TextureAtlasInjection.class, "getTextures");
    }
}
