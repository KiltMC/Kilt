package xyz.bluspring.kilt.forgeinjects.client.gui.components.toasts;

import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastComponent.class)
public class ToastComponentInject {
    @Inject(at = @At("HEAD"), method = "addToast", cancellable = true)
    public void kilt$addForgeToast(Toast toast, CallbackInfo ci) {
        if (ForgeHooksClient.onToastAdd(toast))
            ci.cancel();
    }
}
