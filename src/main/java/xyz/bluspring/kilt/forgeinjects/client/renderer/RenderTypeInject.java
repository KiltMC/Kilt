// TRACKED HASH: 2b151de67f98ea81974b31520a3345227de93180
package xyz.bluspring.kilt.forgeinjects.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ForgeRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.client.RenderTypeInjection;

@Mixin(RenderType.class)
public abstract class RenderTypeInject implements RenderTypeInjection {
    @Unique
    private int chunkLayerId = -1;

    @Override
    public int getChunkLayerId() {
        return chunkLayerId;
    }

    @Override
    public void setChunkLayerId(int id) {
        chunkLayerId = id;
    }

    @Inject(at = @At("RETURN"), method = "text", cancellable = true)
    private static void kilt$text(ResourceLocation resourceLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(ForgeRenderTypes.getText(resourceLocation));
    }

    @Inject(at = @At("RETURN"), method = "textIntensity", cancellable = true)
    private static void kilt$textIntensity(ResourceLocation resourceLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(ForgeRenderTypes.getTextIntensity(resourceLocation));
    }

    @Inject(at = @At("RETURN"), method = "textIntensityPolygonOffset", cancellable = true)
    private static void kilt$textIntensityPolygonOffset(ResourceLocation resourceLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(ForgeRenderTypes.getTextIntensityPolygonOffset(resourceLocation));
    }

    @Inject(at = @At("RETURN"), method = "textPolygonOffset", cancellable = true)
    private static void kilt$textPolygonOffset(ResourceLocation resourceLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(ForgeRenderTypes.getTextPolygonOffset(resourceLocation));
    }

    @Inject(at = @At("RETURN"), method = "textSeeThrough", cancellable = true)
    private static void kilt$textSeeThrough(ResourceLocation resourceLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(ForgeRenderTypes.getTextSeeThrough(resourceLocation));
    }

    @Inject(at = @At("RETURN"), method = "textIntensitySeeThrough", cancellable = true)
    private static void kilt$textIntensitySeeThrough(ResourceLocation resourceLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(ForgeRenderTypes.getTextIntensitySeeThrough(resourceLocation));
    }

    static {
        var i = 0;
        for (var layer : RenderType.chunkBufferLayers())
            ((RenderTypeInjection) layer).setChunkLayerId(i++);
    }
}