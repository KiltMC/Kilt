package xyz.bluspring.kilt.injects.blaze3d.textures;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.textures.TextureFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.blaze3d.textures.TextureFormatInjection;

@Mixin(TextureFormat.class)
public enum TextureFormatInject implements TextureFormatInjection {
    DEPTH24_STENCIL8(4),
    DEPTH32_STENCIL8(8),
    ;

    @Shadow
    TextureFormatInject(int pixelSize) {}

    @ModifyReturnValue(method = "hasDepthAspect", at = @At("RETURN"))
    private boolean kilt$checkWithNeoDepth(boolean original) {
        return original || this == DEPTH24_STENCIL8 || this == DEPTH32_STENCIL8;
    }

    @Override
    public boolean hasStencilAspect() {
        return this == DEPTH24_STENCIL8 || this == DEPTH32_STENCIL8;
    }
}
