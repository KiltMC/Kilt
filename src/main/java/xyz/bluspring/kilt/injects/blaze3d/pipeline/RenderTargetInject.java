package xyz.bluspring.kilt.injects.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.TextureFormat;
import net.neoforged.neoforge.client.ClientHooks;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.blaze3d.pipeline.RenderTargetInjection;

@Mixin(RenderTarget.class)
public abstract class RenderTargetInject implements RenderTargetInjection {
    @Shadow
    @Final
    public boolean useDepth;
    @Unique public boolean useStencil;

    @Override
    public boolean kilt$useStencil() {
        return this.useStencil;
    }

    @Override
    public void kilt$setStencil(boolean useStencil) {
        if (useStencil && !this.useDepth)
            throw new IllegalArgumentException("Stencil can only be enabled if depth is enabled.");

        this.useStencil = useStencil;
    }

    public RenderTargetInject(@Nullable String label, boolean useDepth) {}

    @CreateInitializer
    public RenderTargetInject(@Nullable String label, boolean useDepth, boolean useStencil) {
        this(label, useDepth);
        this.kilt$setStencil(useStencil);
    }

    @ModifyArg(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuDevice;createTexture(Ljava/util/function/Supplier;ILcom/mojang/blaze3d/textures/TextureFormat;IIII)Lcom/mojang/blaze3d/textures/GpuTexture;"))
    private TextureFormat kilt$tryUseStencilTexture(TextureFormat format) {
        if (this.useStencil)
            return ClientHooks.getStencilFormat();

        return format;
    }
}
