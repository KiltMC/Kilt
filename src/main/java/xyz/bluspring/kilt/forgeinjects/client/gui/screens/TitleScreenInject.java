package xyz.bluspring.kilt.forgeinjects.client.gui.screens;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.gui.TitleScreenModUpdateIndicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenInject extends Screen {
    private TitleScreenModUpdateIndicator modUpdateNotification;

    protected TitleScreenInject(Component title) {
        super(title);
    }

    // Kilt: handled by ModMenu

    @Inject(method = "init", at = @At("TAIL"))
    private void kilt$initModUpdateNotification(CallbackInfo ci) {
        modUpdateNotification = TitleScreenModUpdateIndicator.init((TitleScreen) (Object) this, null);
    }

    @Definition(id = "splash", field = "Lnet/minecraft/client/gui/screens/TitleScreen;splash:Lnet/minecraft/client/gui/components/SplashRenderer;")
    @Expression("this.splash != null")
    @Inject(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$handleRenderMainMenu(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci, @Local(ordinal = 2) int color) {
        ForgeHooksClient.renderMainMenu((TitleScreen) (Object) this, guiGraphics, this.font, this.width, this.height, color);
    }

    /*@Definition(id = "g", local = @Local(type = float.class, ordinal = 2))
    @Expression("g >= 1.0") // this is unironically the only place I could capture this local
    @Inject(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$renderModUpdateNotification(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci, @Local(ordinal = 2) float f) {
        if (f >= 1f) {
            this.modUpdateNotification.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }*/
}
