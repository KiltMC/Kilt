// TRACKED HASH: 0352f8e699c8b1e9b6ac77b9927e3852ffaf311c
package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.renderer.ScreenEffectRendererInjection;
import xyz.bluspring.kilt.mixin.ScreenEffectRendererAccessor;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererInject implements ScreenEffectRendererInjection {
    @CreateStatic
    private static AtomicReference<ResourceLocation> currentTexture = ScreenEffectRendererInjection.currentTexture;

    @CreateStatic
    private static void renderFluid(Minecraft mc, PoseStack poseStack, ResourceLocation texture) {
        ScreenEffectRendererInjection.renderFluid(mc, poseStack, texture);
    }

    @WrapOperation(method = "renderWater", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"))
    private static void kilt$useForgeWaterRender(int i, ResourceLocation resourceLocation, Operation<Void> original) {
        original.call(i, ScreenEffectRendererInjection.currentTexture.get());
    }

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void kilt$setUnderwaterTexture(CallbackInfo ci) {
        ScreenEffectRendererInjection.currentTexture.set(ScreenEffectRendererAccessor.getUnderwaterLocation());
    }
}