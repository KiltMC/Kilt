package xyz.bluspring.kilt.forgeinjects.client.renderer.texture;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.ForgeHooksClient;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasInject {
    @Shadow protected abstract ResourceLocation getResourceLocation(ResourceLocation resourceLocation);

    @Inject(method = "prepareToStitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void kilt$runPreStitchEvent(ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i, CallbackInfoReturnable<TextureAtlas.Preparations> cir, Set<ResourceLocation> set, int j, Stitcher stitcher, int k, int l) {
        ForgeHooksClient.onTextureStitchedPre((TextureAtlas) (Object) this, set);
    }

    @Inject(at = @At("TAIL"), method = "reload")
    public void kilt$runPostStitchEvent(TextureAtlas.Preparations preparations, CallbackInfo ci) {
        ForgeHooksClient.onTextureStitchedPost((TextureAtlas) (Object) this);
    }

    @ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite$Info;IIIII)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;")
    public TextureAtlasSprite kilt$useForgeTextureSprite(
            TextureAtlasSprite atlasSprite,
            ResourceManager resourceManager,
            TextureAtlasSprite.Info info,
            @Local(ordinal = 0) int i,
            @Local(ordinal = 1) int j,
            @Local(ordinal = 2) int k,
            @Local(ordinal = 3) int l,
            @Local(ordinal = 4) int m
    ) {
        var resourceLocation = this.getResourceLocation(info.name());
        try {
            var resource = resourceManager.getResourceOrThrow(resourceLocation);
            var inputStream = resource.open();
            var nativeImage = NativeImage.read(inputStream);

            var forgeTextureSprite = ForgeHooksClient.loadTextureAtlasSprite(
                    (TextureAtlas) (Object) this, resourceManager,
                    info, resource,
                    i, j,
                    l, m,
                    k, nativeImage
            );

            if (forgeTextureSprite == null)
                return atlasSprite;

            return forgeTextureSprite;
        } catch (IOException e) {
            e.printStackTrace();
            return atlasSprite;
        }
    }
}
