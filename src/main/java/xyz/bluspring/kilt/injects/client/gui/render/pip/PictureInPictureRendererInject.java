package xyz.bluspring.kilt.injects.client.gui.render.pip;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.injections.client.gui.render.pip.PictureInPictureRendererInjection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

@Mixin(PictureInPictureRenderer.class)
public abstract class PictureInPictureRendererInject<T extends PictureInPictureRenderState> implements PictureInPictureRendererInjection<T> {
    @Shadow private @Nullable GpuTexture texture;

    @ModifyArg(method = "prepareTexturesAndProjection", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuDevice;createTexture(Ljava/util/function/Supplier;ILcom/mojang/blaze3d/textures/TextureFormat;IIII)Lcom/mojang/blaze3d/textures/GpuTexture;"))
    private TextureFormat kilt$tryCopyDepthStencilSetting(TextureFormat format) {
        if (format != TextureFormat.DEPTH32) // Kilt: prioritize Fabric mods
            return format;

        return Minecraft.getInstance().getMainRenderTarget().getDepthTexture().getFormat();
    }

    @Override
    public boolean canBeReusedFor(T state, int textureWidth, int textureHeight) {
        return this.texture == null || (this.texture.getWidth(0) == textureWidth && this.texture.getHeight(0) == textureHeight);
    }
}
