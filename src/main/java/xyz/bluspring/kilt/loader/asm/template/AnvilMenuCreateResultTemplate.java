package xyz.bluspring.kilt.loader.asm.template;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.inventory.AnvilMenuInjection;

@Pseudo // Pseudo is needed to avoid compile error, we remove it when generating the actual mixin.
@Mixin(targets = {""}, priority = 1050) // We populate targets at runtime.
public abstract class AnvilMenuCreateResultTemplate implements AnvilMenuInjection {
    @Inject(method = "createResult", at = @At("RETURN"))
    private void kilt$callAnvilUpdateEvent(CallbackInfo ci) {
        this.kilt$handleUpdateEvent();
    }
}
