// TRACKED HASH: ca60bbd06c7c885c4461c9ed6a53102032e79acf
package xyz.bluspring.kilt.injects.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MipmapGenerator.class)
public abstract class MipmapGeneratorInject {
    @Shadow private static boolean hasTransparentPixel(NativeImage image) {
        throw new IllegalStateException();
    };
    @Shadow private static int alphaBlend(int col0, int col1, int col2, int col3, boolean transparent) {
        throw new IllegalStateException();
    };


    /**
     * @author BluSpring
     * @reason no other way to add a continue, other than writing a fuckin' mixin plugin for it, or doing some weird workarounds.
     */
    @Overwrite
    public static NativeImage[] generateMipLevels(NativeImage[] images, int mipLevel) {
        if (mipLevel + 1 <= images.length) {
            return images;
        } else {
            NativeImage[] nativeImages = new NativeImage[mipLevel + 1];
            nativeImages[0] = images[0];
            boolean bl = hasTransparentPixel(nativeImages[0]);

            int maxMipLevel = ClientHooks.getMaxMipmapLevel(nativeImages[0].getWidth(), nativeImages[0].getHeight());
            for(int i = 1; i <= mipLevel; ++i) {
                if (i < images.length) {
                    nativeImages[i] = images[i];
                } else {
                    NativeImage nativeImage = nativeImages[i - 1];
                    NativeImage nativeImage2 = new NativeImage(Math.max(1, nativeImage.getWidth() >> 1), Math.max(1, nativeImage.getHeight() >> 1), false);;
                    if (i <= maxMipLevel) {
                        int j = nativeImage2.getWidth();
                        int k = nativeImage2.getHeight();

                        for (int l = 0; l < j; ++l) {
                            for (int m = 0; m < k; ++m) {
                                nativeImage2.setPixelRGBA(l, m, alphaBlend(nativeImage.getPixelRGBA(l * 2 + 0, m * 2 + 0), nativeImage.getPixelRGBA(l * 2 + 1, m * 2 + 0), nativeImage.getPixelRGBA(l * 2 + 0, m * 2 + 1), nativeImage.getPixelRGBA(l * 2 + 1, m * 2 + 1), bl));
                            }
                        }
                    }

                    nativeImages[i] = nativeImage2;
                }
            }

            return nativeImages;
        }
    }

}