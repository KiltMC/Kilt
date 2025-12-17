// TRACKED HASH: 38ed9a92a3865ea4db1c28fa04a9e858ee73d618
package xyz.bluspring.kilt.injects.client.renderer.texture;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(SpriteLoader.class)
public abstract class SpriteLoaderInject {
    @Shadow @Final private ResourceLocation location;

    @Redirect(method = "stitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;log2(I)I", ordinal = 2))
    private int kilt$avoidMipmapLowering(int value, @Local(argsOnly = true) int mipLevel) {
        return mipLevel;
    }
}