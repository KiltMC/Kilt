// TRACKED HASH: db725119dbf943686f97f078ada41b52bcc6aef3
package xyz.bluspring.kilt.injects.client.renderer.texture;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.textures.ForgeTextureMetadata;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.renderer.texture.SpriteContentsInjection;

@Mixin(SpriteContents.class)
public abstract class SpriteContentsInject implements SpriteContentsInjection {
    @Shadow @Final private NativeImage originalImage;
    @Shadow @Final int width;
    @Shadow @Final int height;

    @Nullable
    public ForgeTextureMetadata forgeMeta;

    public SpriteContentsInject(ResourceLocation name, FrameSize frameSize, NativeImage originalImage, AnimationMetadataSection metadata) {}

    @CreateInitializer
    public SpriteContentsInject(ResourceLocation name, FrameSize frameSize, NativeImage originalImage, AnimationMetadataSection metadata, @Nullable ForgeTextureMetadata forgeMeta) {
        this.forgeMeta = forgeMeta;
    }

    @Override
    public NativeImage getOriginalImage() {
        return this.originalImage;
    }

    @Override
    public ForgeTextureMetadata kilt$getForgeMeta() {
        return this.forgeMeta;
    }

    @Override
    public void kilt$setForgeMeta(ForgeTextureMetadata metadata) {
        this.forgeMeta = metadata;
    }

    @Inject(method = "upload", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;upload(IIIIIIIZZ)V", ordinal = 0, shift = At.Shift.BEFORE), cancellable = true)
    private void kilt$skipUploadIfInvalidMipLevel(CallbackInfo ci, @Local(ordinal = 4) int i) {
        if ((this.width >> i) <= 0 || (this.height >> i) <= 0)
            ci.cancel();
    }

    @Mixin(targets = "net.minecraft.client.renderer.texture.SpriteContents$InterpolationData")
    public static class InterpolationDataInject {
        @ModifyArgs(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;<init>(IIZ)V"))
        private void kilt$guardFromInvalidTexSize(Args args) {
            args.set(0, Math.max(1, args.get(0)));
            args.set(1, Math.max(1, args.get(1)));
        }
    }
}