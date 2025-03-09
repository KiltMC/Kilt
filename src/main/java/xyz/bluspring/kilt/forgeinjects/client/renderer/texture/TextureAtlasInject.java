package xyz.bluspring.kilt.forgeinjects.client.renderer.texture;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.textures.ForgeTextureMetadata;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.fml.ModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasInject {
    @Shadow protected abstract ResourceLocation getResourceLocation(ResourceLocation resourceLocation);

    @Inject(method = "prepareToStitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 1, shift = At.Shift.BEFORE))
    private void kilt$disableMipLowering(ResourceManager resourceManager, Stream<ResourceLocation> spriteNames, ProfilerFiller profiler, int mipLevel, CallbackInfoReturnable<TextureAtlas.Preparations> cir, @Local(ordinal = 6) LocalIntRef currentLevel) {
        if (!ForgeConfig.CLIENT.allowMipmapLowering()) {
            currentLevel.set(mipLevel);
        }
    }

    @Inject(method = "prepareToStitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.AFTER))
    public void kilt$runPreStitchEvent(ResourceManager resourceManager, Stream<ResourceLocation> stream, ProfilerFiller profilerFiller, int i, CallbackInfoReturnable<TextureAtlas.Preparations> cir, @Local Set<ResourceLocation> set) {
        ForgeHooksClient.onTextureStitchedPre((TextureAtlas) (Object) this, set);
    }

    @Inject(at = @At("TAIL"), method = "reload")
    public void kilt$runPostStitchEvent(TextureAtlas.Preparations preparations, CallbackInfo ci) {
        ModLoader.get().postEvent(new TextureStitchEvent.Post((TextureAtlas) (Object) this));
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

            ForgeTextureMetadata metadata = ForgeTextureMetadata.forResource(resource);
            var forgeTextureSprite = metadata.getLoader() == null ? null : metadata.getLoader().load((TextureAtlas) (Object) this, resourceManager, info, resource, i, j, l, m, k, nativeImage);

            if (forgeTextureSprite == null)
                return atlasSprite;

            return forgeTextureSprite;
        } catch (IOException e) {
            e.printStackTrace();
            return atlasSprite;
        }
    }
}
