package xyz.bluspring.kilt.forgeinjects.client.renderer.texture;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasInject {
    @Shadow private Map<ResourceLocation, TextureAtlasSprite> texturesByName;

    public Set<ResourceLocation> getTextureLocations() {
        return Collections.unmodifiableSet(texturesByName.keySet());
    }
}
