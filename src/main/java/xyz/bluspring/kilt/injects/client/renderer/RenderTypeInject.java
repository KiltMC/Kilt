// TRACKED HASH: 2b151de67f98ea81974b31520a3345227de93180
package xyz.bluspring.kilt.injects.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.client.renderer.RenderTypeInjection;

@Mixin(RenderType.class)
public abstract class RenderTypeInject implements RenderTypeInjection {
    @Unique
    private int chunkLayerId = -1;

    @Override
    public int getChunkLayerId() {
        RenderTypeInjection.kilt$initLoadedChunkLayers(); // Kilt: try to ensure chunk layers are actually all loaded.
        return chunkLayerId;
    }

    @Override
    public void setChunkLayerId(int id) {
        chunkLayerId = id;
    }

    @Inject(at = @At("RETURN"), method = "text", cancellable = true)
    private static void kilt$text(ResourceLocation resourceLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(NeoForgeRenderTypes.getText(resourceLocation));
    }

    @Inject(at = @At("RETURN"), method = "textIntensity", cancellable = true)
    private static void kilt$textIntensity(ResourceLocation resourceLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(NeoForgeRenderTypes.getTextIntensity(resourceLocation));
    }

    @Inject(at = @At("RETURN"), method = "textIntensityPolygonOffset", cancellable = true)
    private static void kilt$textIntensityPolygonOffset(ResourceLocation resourceLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(NeoForgeRenderTypes.getTextIntensityPolygonOffset(resourceLocation));
    }

    @Inject(at = @At("RETURN"), method = "textPolygonOffset", cancellable = true)
    private static void kilt$textPolygonOffset(ResourceLocation resourceLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(NeoForgeRenderTypes.getTextPolygonOffset(resourceLocation));
    }

    @Inject(at = @At("RETURN"), method = "textSeeThrough", cancellable = true)
    private static void kilt$textSeeThrough(ResourceLocation resourceLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(NeoForgeRenderTypes.getTextSeeThrough(resourceLocation));
    }

    @Inject(at = @At("RETURN"), method = "textIntensitySeeThrough", cancellable = true)
    private static void kilt$textIntensitySeeThrough(ResourceLocation resourceLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(NeoForgeRenderTypes.getTextIntensitySeeThrough(resourceLocation));
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$initChunkLayerIds(CallbackInfo ci) {
        RenderTypeInjection.kilt$initLoadedChunkLayers();
    }
}