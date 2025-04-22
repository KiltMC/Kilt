package xyz.bluspring.kilt.forgeinjects.server.dedicated;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.dedicated.ServerWatchdog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerWatchdog.class)
public abstract class ServerWatchdogInject {
    @ModifyExpressionValue(method = "run", at = @At(value = "CONSTANT", args = "stringValue=Watchdog"))
    private String kilt$formatWatchdogCrash(String original, @Local(ordinal = 2) long k) {
        return original + String.format(java.util.Locale.ENGLISH, ": ServerHangWatchdog detected that a single server tick took %.2f seconds (should be max 0.05)", k / 1000F);
    }
}
