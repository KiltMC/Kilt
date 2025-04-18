package xyz.bluspring.kilt.compat.curios_trinkets.mixin.trinkets;

import dev.emi.trinkets.TrinketScreenManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.compat.curios_trinkets.KiltCTCompatConfig;

@Mixin(TrinketScreenManager.class)
public abstract class TrinketScreenManagerMixin {
    @Inject(method = {"update", "drawExtraGroups", "drawActiveGroup"}, at = @At("HEAD"), cancellable = true, remap = false)
    private static void kilt$compat$tc$disableTrinketsInventoryHandling(CallbackInfo ci) {
        if (!KiltCTCompatConfig.INSTANCE.getGuiMode().get().isTrinkets())
            ci.cancel();
    }
}
