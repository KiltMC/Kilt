package xyz.bluspring.kilt.injects.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.textures.TextureFormat;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.blaze3d.pipeline.RenderTargetInjection;

@Mixin(MainTarget.class)
public abstract class MainTargetInject implements RenderTargetInjection {
    @Shadow
    protected abstract void createFrameBuffer(int desiredWidth, int desiredHeight);

    public MainTargetInject(int desiredWidth, int desiredHeight) {}

    @CreateInitializer
    public MainTargetInject(int desiredWidth, int desiredHeight, boolean enableStencil) {
        this(desiredWidth, desiredHeight);
        this.kilt$setStencil(enableStencil);

        if (enableStencil)
            this.createFrameBuffer(desiredWidth, desiredHeight); // Kilt: create again to make sure we have stencil.
    }

    @ModifyArg(method = "allocateDepthAttachment", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuDevice;createTexture(Ljava/util/function/Supplier;ILcom/mojang/blaze3d/textures/TextureFormat;IIII)Lcom/mojang/blaze3d/textures/GpuTexture;"))
    private TextureFormat kilt$tryUseStencilTexture(TextureFormat format) {
        if (this.kilt$useStencil())
            return ClientHooks.getStencilFormat();

        return format;
    }
}
