package xyz.bluspring.kilt.injects.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RenderTarget.class)
public abstract class RenderTargetInject {
    // Kilt: Handled by Porting Lib
}
