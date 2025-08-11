// TRACKED HASH: 8f33a52b76e5b789f7deea7c3fd48392b0222224
package xyz.bluspring.kilt.injects.client.renderer.texture;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.renderer.texture.SpriteContentsInjection;
import xyz.bluspring.kilt.injections.client.renderer.texture.TextureAtlasSpriteInjection;

@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteInject implements TextureAtlasSpriteInjection {
    @Shadow @Final private SpriteContents contents;

    @Override
    public int getPixelRGBA(int frameIndex, int x, int y) {
        if (this.contents.animatedTexture != null) {
            x += this.contents.animatedTexture.getFrameX(frameIndex) * this.contents.width();
            y += this.contents.animatedTexture.getFrameY(frameIndex) * this.contents.width();
        }

        return ((SpriteContentsInjection) this.contents).getOriginalImage().getPixelRGBA(x, y);
    }
}