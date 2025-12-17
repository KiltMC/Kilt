// TRACKED HASH: cd198128a8a3f09227e99d840f8241904533cac2
package xyz.bluspring.kilt.injects.client.gui.components;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayInject {
    @Shadow @Final private Minecraft minecraft;

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;drawBar(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/world/BossEvent;)V"))
    private boolean kilt$customizeBossEventProgress(BossHealthOverlay instance, GuiGraphics guiGraphics, int x, int y, BossEvent bossEvent, @Local(ordinal = 2) int k, @Local(ordinal = 1) int j, @Share("event") LocalRef<CustomizeGuiOverlayEvent.BossEventProgress> eventRef) {
        var event = ClientHooks.onCustomizeBossEventProgress(guiGraphics, this.minecraft.getWindow(), (LerpingBossEvent) bossEvent, k, j, 10 + this.minecraft.font.lineHeight);
        eventRef.set(event);
        return !event.isCanceled();
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
    private int kilt$cancelStringIfCancelled(GuiGraphics instance, Font font, Component text, int x, int y, int color, Operation<Integer> original, @Share("event") LocalRef<CustomizeGuiOverlayEvent.BossEventProgress> eventRef) {
        if (!eventRef.get().isCanceled()) {
            return original.call(instance, font, text, x, y, color);
        }

        return 0;
    }

    @Expression("10 + 9")
    @ModifyExpressionValue(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int kilt$getIncrement(int original, @Share("event") LocalRef<CustomizeGuiOverlayEvent.BossEventProgress> eventRef) {
        var increment = eventRef.get().getIncrement();

        if (original != 19)
            return original;

        return increment;
    }
}