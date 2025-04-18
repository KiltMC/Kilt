package xyz.bluspring.kilt.compat.curios_trinkets.mixin.curios;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.client.gui.GuiEventHandler;

@Mixin(value = GuiEventHandler.class, remap = false)
public abstract class GuiEventHandlerMixin {
    @Inject(method = {"onInventoryGuiInit", "onInventoryGuiDrawBackground", "onMouseClick"}, at = @At("HEAD"), cancellable = true)
    private void kilt$tc$disableCuriosInventoryHandling(CallbackInfo ci) {
        ci.cancel();
    }
}
