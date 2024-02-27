// TRACKED HASH: cd198128a8a3f09227e99d840f8241904533cac2
package xyz.bluspring.kilt.forgeinjects.client.gui.components;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayInject {
    @Shadow @Final private Minecraft minecraft;

    // TODO: figure out how to wrap blocks of code in an event condition
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;drawBar(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/world/BossEvent;)V", shift = At.Shift.BEFORE))
    private void kilt$customizeBossEventProgress(GuiGraphics guiGraphics, CallbackInfo ci, @Local LerpingBossEvent bossEvent, @Local(name = "k") int k, @Local(name = "j") int j) {
        ForgeHooksClient.onCustomizeBossEventProgress(guiGraphics, this.minecraft.getWindow(), bossEvent, k, j, 10 + this.minecraft.font.lineHeight);
    }
}