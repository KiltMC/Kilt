package xyz.bluspring.kilt.mixin.compat.forgeconfigapiport;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = ConfigTracker.class, remap = false)
public abstract class ConfigTrackerMixin {
    @Shadow @Final private ConcurrentHashMap<String, ModConfig> fileMap;
    @Shadow @Final private static Logger LOGGER;

    @WrapOperation(method = "openConfig", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/config/ModConfig;save()V"))
    private void kilt$avoidSavingCrashWithNPE(ModConfig instance, Operation<Void> original) {
        // Kilt: Due to ForgeConfigAPIPort loading the config immediately, this ends up resulting in TACZ Forge crashing immediately
        //       on startup. To combat this, we have to catch the NPE when it occurs, and hopefully this doesn't corrupt any state.
        try {
            original.call(instance);
        } catch (NullPointerException e) {
            Kilt.Companion.getLogger().error("An error occurred whilst saving mod config, but we're hoping that this is a false alarm!");
            e.printStackTrace();
        }
    }

    @Inject(method = "trackConfig", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Lorg/slf4j/Marker;Ljava/lang/String;[Ljava/lang/Object;)V", shift = At.Shift.AFTER), cancellable = true)
    private void kilt$avoidDoubleTrackingCrash(ModConfig config, CallbackInfo ci) {
        if (this.fileMap.get(config.getFileName()) == config) {
            LOGGER.warn("Kilt: Because the config sources match, we're going to ignore the error.");
            ci.cancel();
        }
    }
}
