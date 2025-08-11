// TRACKED HASH: 9e33c1025a3eb31abc0c9a1359436e9e8c853995
package xyz.bluspring.kilt.injects.client.renderer;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class FogRendererInject {
    @Shadow private static float fogRed;
    @Shadow private static float fogGreen;
    @Shadow private static float fogBlue;

    @Inject(method = "setupColor", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V", ordinal = 1))
    private static void kilt$modifyFogColors(Camera activeRenderInfo, float partialTicks, ClientLevel level, int renderDistanceChunks, float bossColorModifier, CallbackInfo ci) {
        var fogColor = ClientHooks.getFogColor(activeRenderInfo, partialTicks, level, renderDistanceChunks, bossColorModifier, fogRed, fogGreen, fogBlue);

        fogRed = fogColor.x();
        fogGreen = fogColor.y();
        fogBlue = fogColor.z();
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void kilt$callFogRenderEvent(Camera camera, FogRenderer.FogMode fogMode, float farPlaneDistance, boolean bl, float f, CallbackInfo ci, @Local FogRenderer.FogData fogData, @Local FogType fogType) {
        ClientHooks.onFogRender(fogMode, fogType, camera, f, farPlaneDistance, fogData.start, fogData.end, fogData.shape);
    }
}