package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.render.ScreenEffectRendererInjection;
import xyz.bluspring.kilt.mixin.ScreenEffectRendererAccessor;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererInject implements ScreenEffectRendererInjection {
    @Redirect(method = "renderWater", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"))
    private static void kilt$useForgeWaterRender(int i, ResourceLocation resourceLocation) {
        RenderSystem.setShaderTexture(i, ScreenEffectRendererInjection.currentTexture.get());
    }

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void kilt$setUnderwaterTexture(CallbackInfo ci) {
        ScreenEffectRendererInjection.currentTexture.set(ScreenEffectRendererAccessor.getUnderwaterLocation());
    }
}
