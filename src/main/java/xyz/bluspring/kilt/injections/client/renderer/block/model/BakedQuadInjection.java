package xyz.bluspring.kilt.injections.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public interface BakedQuadInjection {
    default boolean hasAmbientOcclusion() {
        throw new IllegalStateException();
    }

    default void kilt$setAmbientOcclusion(boolean hasAo) {
        throw new IllegalStateException();
    }

    static BakedQuad withAo(int[] is, int i, Direction direction, TextureAtlasSprite textureAtlasSprite, boolean bl, boolean hasAo) {
        var quad = new BakedQuad(is, i, direction, textureAtlasSprite, bl);
        ((BakedQuadInjection) quad).kilt$setAmbientOcclusion(hasAo);

        return quad;
    }
}
