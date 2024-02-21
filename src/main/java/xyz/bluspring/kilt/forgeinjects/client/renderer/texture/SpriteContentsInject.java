package xyz.bluspring.kilt.forgeinjects.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.textures.ForgeTextureMetadata;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.renderer.texture.SpriteContentsInjection;

@Mixin(SpriteContents.class)
public class SpriteContentsInject implements SpriteContentsInjection {
    @Shadow @Final private NativeImage originalImage;
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
}
