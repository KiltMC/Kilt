// TRACKED HASH: 38ed9a92a3865ea4db1c28fa04a9e858ee73d618
package xyz.bluspring.kilt.injects.client.renderer.texture;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.resources.ResourceLocation;

@Mixin(SpriteLoader.class)
public abstract class SpriteLoaderInject {
    @Shadow @Final private ResourceLocation location;

    @Redirect(method = "stitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;log2(I)I", ordinal = 2), require = 0, expect = 0)
    private int kilt$avoidMipmapLowering(int value, @Local(argsOnly = true) int mipLevel) {
        return mipLevel;
    }
}
