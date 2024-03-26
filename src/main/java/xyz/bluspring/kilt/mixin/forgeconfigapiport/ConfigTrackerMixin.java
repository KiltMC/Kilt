package xyz.bluspring.kilt.mixin.forgeconfigapiport;

import io.github.fabricators_of_create.porting_lib.config.ConfigTracker;
import io.github.fabricators_of_create.porting_lib.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ConfigTracker.class, remap = false)
public class ConfigTrackerMixin {
    @Inject(method = "trackConfig", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Lorg/slf4j/Marker;Ljava/lang/String;[Ljava/lang/Object;)V", remap = false, shift = At.Shift.AFTER), remap = false, cancellable = true)
    private void kilt$avoidCrashOnConflict(ModConfig config, CallbackInfo ci) {
        ci.cancel();
    }
}
