package xyz.bluspring.kilt.mixin.compat.forgeconfigapiport;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.Kilt;

@Mixin(value = ConfigTracker.class, remap = false)
public abstract class ConfigTrackerMixin {
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
}
