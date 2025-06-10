package xyz.bluspring.kilt.mixin.compat.unionlib;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@IfModLoaded("unionlib")
@Mixin(targets = "com.stereowalker.unionlib.fabric.PlatformHelper")
public class PlatformHelperMixin {
    @Dynamic
    @Inject(method = "lambda$handleClientEvents$5", at = @At("HEAD"), cancellable = true)
    private static void kilt$unionlib$avoidScreenCrash(Minecraft client, Screen screen, int scaledWidth, int scaledHeight, CallbackInfo ci) {
        if (Minecraft.getInstance().screen == null)
            ci.cancel();
    }
}
