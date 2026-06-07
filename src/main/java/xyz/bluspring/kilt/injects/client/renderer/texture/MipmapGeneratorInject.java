// TRACKED HASH: ca60bbd06c7c885c4461c9ed6a53102032e79acf
package xyz.bluspring.kilt.injects.client.renderer.texture;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.platform.NativeImage;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import net.minecraft.client.renderer.texture.MipmapGenerator;

@Mixin(value = MipmapGenerator.class, priority = 1050)
public abstract class MipmapGeneratorInject {
    @Inject(method = "generateMipLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/MipmapGenerator;hasTransparentPixel(Lcom/mojang/blaze3d/platform/NativeImage;)Z", shift = At.Shift.AFTER))
    private static void kilt$getMaxMipLevel(NativeImage[] images, int mipLevel, CallbackInfoReturnable<NativeImage[]> cir, @Local(ordinal = 1) NativeImage[] nativeImages, @Share("maxMipLevel") LocalIntRef maxMipLevel) {
        maxMipLevel.set(ClientHooks.getMaxMipmapLevel(nativeImages[0].getWidth(), nativeImages[0].getHeight()));
    }

    @ModifyArgs(method = "generateMipLevels", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;<init>(IIZ)V"))
    private static void kilt$guardFromInvalidTexSize(Args args) {
        args.set(0, Math.max(1, args.get(0)));
        args.set(1, Math.max(1, args.get(1)));
    }

    @Definition(id = "l", local = @Local(type = int.class, ordinal = 4))
    @Definition(id = "j", local = @Local(type = int.class, ordinal = 2))
    @Expression("l < j")
    @ModifyExpressionValue(method = "generateMipLevels", at = @At("MIXINEXTRAS:EXPRESSION"), require = 0, expect = 0)
    private static boolean kilt$avoidBlendingPixelsIfInvalidMip(boolean original, @Local(ordinal = 1) int currentMipLevel, @Share("maxMipLevel") LocalIntRef maxMipLevel) {
        if (currentMipLevel > maxMipLevel.get())
            return false;

        return original;
    }
}
