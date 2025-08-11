package xyz.bluspring.kilt.injects.client.gui.screens;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenInject extends Screen {
    protected TitleScreenInject(Component title) {
        super(title);
    }
    // Kilt: handled by ModMenu

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/LogoRenderer;renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IF)V", shift = At.Shift.AFTER))
    private void kilt$callRenderMainMenu(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci, @Local(ordinal = 2) int i) {
        ClientHooks.renderMainMenu((TitleScreen) (Object) this, guiGraphics, this.font, this.width, this.height, i);
    }
}
