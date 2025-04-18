package xyz.bluspring.kilt.compat.curios_trinkets.mixin.curios;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.client.gui.GuiEventHandler;
import xyz.bluspring.kilt.compat.curios_trinkets.KiltCTCompatConfig;

@Mixin(value = GuiEventHandler.class, remap = false)
public abstract class GuiEventHandlerMixin {
    @Inject(method = {"onInventoryGuiInit", "onInventoryGuiDrawBackground", "onMouseClick"}, at = @At("HEAD"), cancellable = true)
    private void kilt$compat$tc$disableCuriosInventoryHandling(CallbackInfo ci) {
        if (!KiltCTCompatConfig.INSTANCE.getGuiMode().get().isCurios())
            ci.cancel();
    }
}
