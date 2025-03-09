package xyz.bluspring.kilt.forgeinjects.client.renderer.texture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteInject {

    @Mixin(targets = "net.minecraft.client.renderer.texture.TextureAtlasSprite.InterpolationData")
    public abstract static class InterpolationDataInject {
        @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(IIZ)Lcom/mojang/blaze3d/platform/NativeImage;"))
        private NativeImage kilt$guardInvalidTextureSize(int width, int height, boolean useCalloc, Operation<NativeImage> original) {
            return original.call(Math.max(1, width), Math.max(1, height), useCalloc);
        }
    }
}