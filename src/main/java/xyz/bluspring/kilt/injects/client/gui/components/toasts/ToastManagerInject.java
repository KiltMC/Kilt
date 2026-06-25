// TRACKED HASH: 976f1ac17422a80c6be0cf2e6ec26fc055ebf7b5
package xyz.bluspring.kilt.injects.client.gui.components.toasts;

import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;

@Mixin(ToastManager.class)
public abstract class ToastManagerInject {
    @Inject(at = @At("HEAD"), method = "addToast", cancellable = true)
    public void kilt$addForgeToast(Toast toast, CallbackInfo ci) {
        if (ClientHooks.onToastAdd(toast))
            ci.cancel();
    }
}
