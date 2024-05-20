// TRACKED HASH: cd8ee0093793e18602ef6a39ab262eaddd098eed
package xyz.bluspring.kilt.forgeinjects.client.gui.screens.inventory;

import com.google.common.collect.Ordering;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenInject<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public EffectRenderingInventoryScreenInject(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "renderEffects", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;", shift = At.Shift.BEFORE, remap = false), cancellable = true)
    private void kilt$setScreenPotionSize(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci, @Local(ordinal = 2) LocalIntRef i, @Local(ordinal = 3) int j, @Local LocalBooleanRef flag) {
        var event = ForgeHooksClient.onScreenPotionSize(this, j, !flag.get(), i.get());
        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        flag.set(!event.isCompact());
        i.set(event.getHorizontalOffset());
    }

    @WrapOperation(method = "renderEffects", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;", remap = false))
    private List<MobEffectInstance> kilt$filterOnlyRenderableEffects(Ordering<MobEffectInstance> instance, Iterable<MobEffectInstance> elements, Operation<List<MobEffectInstance>> original) {
        return original.call(instance, elements).stream().filter(ForgeHooksClient::shouldRenderEffect).sorted().collect(Collectors.toList());
    }

    @Inject(method = "renderIcons", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;getEffect()Lnet/minecraft/world/effect/MobEffect;", shift = At.Shift.BEFORE))
    private void kilt$customRenderIconForge(GuiGraphics guiGraphics, int renderX, int yOffset, Iterable<MobEffectInstance> effects, boolean isSmall, CallbackInfo ci, @Local MobEffectInstance effectInstance, @Local(ordinal = 2) LocalIntRef i, @Share("kilt$shouldRender") LocalBooleanRef shouldRender) {
        var renderer = IClientMobEffectExtensions.of(effectInstance);

        if (renderer.renderInventoryIcon(effectInstance, (EffectRenderingInventoryScreen<?>) (Object) this, guiGraphics, renderX + (isSmall ? 6 : 7), i.get(), 0)) {
            i.set(i.get() + yOffset);
            shouldRender.set(false);
            return;
        }

        shouldRender.set(true);
    }

    @WrapWithCondition(method = "renderIcons", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(IIIIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private boolean kilt$disableIconBlitIfCustomRendered(GuiGraphics instance, int x, int y, int blitOffset, int width, int height, TextureAtlasSprite sprite, @Share("kilt$shouldRender") LocalBooleanRef shouldRender) {
        return shouldRender.get();
    }

    @Inject(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen;getEffectName(Lnet/minecraft/world/effect/MobEffectInstance;)Lnet/minecraft/network/chat/Component;", shift = At.Shift.BEFORE))
    private void kilt$customRenderIconForge(GuiGraphics guiGraphics, int renderX, int yOffset, Iterable<MobEffectInstance> effects, CallbackInfo ci, @Local MobEffectInstance effectInstance, @Local(ordinal = 2) LocalIntRef i, @Share("kilt$shouldRender") LocalBooleanRef shouldRender) {
        var renderer = IClientMobEffectExtensions.of(effectInstance);

        if (renderer.renderInventoryText(effectInstance, (EffectRenderingInventoryScreen<?>) (Object) this, guiGraphics, renderX, i.get(), 0)) {
            i.set(i.get() + yOffset);
            shouldRender.set(false);
            return;
        }

        shouldRender.set(true);
    }

    @WrapWithCondition(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
    private boolean kilt$disableTextRenderIfCustomRendered(GuiGraphics instance, Font font, Component text, int x, int y, int color, @Share("kilt$shouldRender") LocalBooleanRef shouldRender) {
        return shouldRender.get();
    }
}