package xyz.bluspring.kilt.mixin.compat.forge.crackerslib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "nonamecrackers2.crackerslib.common.compat.CompatHelper", remap = false)
public abstract class CompatHelperMixin {

    private static final Logger LOGGER = LogManager.getLogger("kilt/CompatHelperMixin");
    private static boolean hasLogged = false; 

    @Inject(method = "areShadersRunning", at = @At("HEAD"), cancellable = true)
    private static void kilt$forceShaders(CallbackInfoReturnable<Boolean> cir) {
        if (!hasLogged) {
            LOGGER.info("[CompatHelperMixin] areShadersRunning() called! Forcing true.");
            hasLogged = true; 
        }
        cir.setReturnValue(true);
    }
}
