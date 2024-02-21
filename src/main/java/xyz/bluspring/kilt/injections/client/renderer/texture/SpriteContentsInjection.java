package xyz.bluspring.kilt.injections.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraftforge.client.textures.ForgeTextureMetadata;

public interface SpriteContentsInjection {
    NativeImage getOriginalImage();
    ForgeTextureMetadata kilt$getForgeMeta();
    void kilt$setForgeMeta(ForgeTextureMetadata metadata);
}
