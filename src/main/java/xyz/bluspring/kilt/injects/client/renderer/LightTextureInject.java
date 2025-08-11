package xyz.bluspring.kilt.injects.client.renderer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightTexture.class)
public abstract class LightTextureInject {
    // Kilt: Handled by Porting Lib
    /*@Definition(id = "l", local = @Local(type = float.class, ordinal = 7))
    @Expression("l > 0.0")
    @Inject(method = "updateLightTexture", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$adjustLightmapColors(float partialTicks, CallbackInfo ci, @Local(ordinal = 0) ClientLevel level, @Local(ordinal = 1) float skyDarken, @Local(ordinal = 1) Vector3f colors) {
        level.effects().adjustLightmapColors(level, partialTicks, skyDarken, skyLight, blockLight, pixelX, pixelY, colors);
    }*/

    // Kilt: We probably shouldn't be fixing this but...
    @ModifyReturnValue(method = "block", at = @At("RETURN"))
    private static int kilt$fixMc169806(int original, @Local(argsOnly = true) int light) {
        return (light & 0xFFFF) >> 4;
    }
}
