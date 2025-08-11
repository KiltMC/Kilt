// TRACKED HASH: 65f2de0de490c09d924d2fe2b4afe2682a6a4ebe
package xyz.bluspring.kilt.injects.client.renderer.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(TextureManager.class)
public abstract class TextureManagerInject {
    @Shadow @Final private Map<ResourceLocation, AbstractTexture> byPath;

    @Inject(method = "release", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;safeClose(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V"))
    private void kilt$fixMc98707(ResourceLocation path, CallbackInfo ci) {
        this.byPath.remove(path);
    }
}