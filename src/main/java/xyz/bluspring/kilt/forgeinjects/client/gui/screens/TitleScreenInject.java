package xyz.bluspring.kilt.forgeinjects.client.gui.screens;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.client.gui.TitleScreenModUpdateIndicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenInject {
    private TitleScreenModUpdateIndicator modUpdateNotification;
    // Kilt: handled by ModMenu

    @Inject(method = "init", at = @At("TAIL"))
    private void kilt$initModUpdateNotification(CallbackInfo ci) {
        modUpdateNotification = TitleScreenModUpdateIndicator.init((TitleScreen) (Object) this, null);
    }
}
