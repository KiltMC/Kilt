// TRACKED HASH: f7771377b9b1ccc4e36e12d9261c92039fef8d18
package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BakedQuadInjection;

@Mixin(BakedQuad.class)
public class BakedQuadInject implements BakedQuadInjection {
    public BakedQuadInject(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade) {}

    @CreateInitializer
    public BakedQuadInject(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, boolean hasAmbientOcclusion) {
        this(vertices, tintIndex, direction, sprite, shade);
        this.hasAmbientOcclusion = hasAmbientOcclusion;
    }

    private boolean hasAmbientOcclusion;

    @Override
    public void kilt$setAmbientOcclusion(boolean hasAo) {
        hasAmbientOcclusion = hasAo;
    }

    @Override
    public boolean hasAmbientOcclusion() {
        return hasAmbientOcclusion;
    }
}