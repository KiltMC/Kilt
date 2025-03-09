package xyz.bluspring.kilt.forgeinjects.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MipmapGenerator.class)
public abstract class MipmapGeneratorInject {
    @Shadow private static int alphaBlend(int col0, int col1, int col2, int col3, boolean transparent) {
        throw new IllegalStateException();
    };


    /**
     * @author BluSpring
     * @reason no other way to add a continue, other than writing a fuckin' mixin plugin for it, or doing some weird workarounds.
     */
    @Overwrite
    public static NativeImage[] generateMipLevels(NativeImage image, int mipLevel) {
        NativeImage[] nativeImages = new NativeImage[mipLevel + 1];
        nativeImages[0] = image;
        if (mipLevel > 0) {
            boolean bl = false;

            label51:
            for(int i = 0; i < image.getWidth(); ++i) {
                for(int j = 0; j < image.getHeight(); ++j) {
                    if (image.getPixelRGBA(i, j) >> 24 == 0) {
                        bl = true;
                        break label51;
                    }
                }
            }

            int maxMipmapLevel = ForgeHooksClient.getMaxMipmapLevel(image.getWidth(), image.getHeight());
            for(int i = 1; i <= mipLevel; ++i) {
                NativeImage nativeImage = nativeImages[i - 1];
                NativeImage nativeImage2 = new NativeImage(Math.max(1, nativeImage.getWidth() >> 1), Math.max(1, nativeImage.getHeight() >> 1), false);

                if (i <= maxMipmapLevel) {
                    int k = nativeImage2.getWidth();
                    int l = nativeImage2.getHeight();

                    for (int m = 0; m < k; ++m) {
                        for (int n = 0; n < l; ++n) {
                            nativeImage2.setPixelRGBA(m, n, alphaBlend(nativeImage.getPixelRGBA(m * 2 + 0, n * 2 + 0), nativeImage.getPixelRGBA(m * 2 + 1, n * 2 + 0), nativeImage.getPixelRGBA(m * 2 + 0, n * 2 + 1), nativeImage.getPixelRGBA(m * 2 + 1, n * 2 + 1), bl));
                        }
                    }
                }

                nativeImages[i] = nativeImage2;
            }
        }

        return nativeImages;
    }

}