package xyz.bluspring.kilt.injects.server.dedicated;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.dedicated.ServerWatchdog;

@Mixin(ServerWatchdog.class)
public abstract class ServerWatchdogInject {
//    @ModifyExpressionValue(method = "run", at = @At(value = "CONSTANT", args = "stringValue=Watchdog"))
//    private String kilt$formatWatchdogCrash(String original, @Local(ordinal = 2) long k) {
//        return original + String.format(java.util.Locale.ENGLISH, ": ServerHangWatchdog detected that a single server tick took %.2f seconds (should be max 0.05)", k / 1000F);
//    }
}
