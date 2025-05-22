package xyz.bluspring.kilt.forgeinjects.client.gui.screens.inventory;

import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenInject<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public EffectRenderingInventoryScreenInject(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Expression("? >= 120")
    @ModifyExpressionValue(method = "renderEffects", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$modifyScreenEffectPositions(boolean original, @Local(ordinal = 3) int availableSpace, @Local(ordinal = 2) LocalIntRef horizontalOffset, @Cancellable CallbackInfo ci) {
        var event = ForgeHooksClient.onScreenPotionSize(this, availableSpace, !original, horizontalOffset.get());

        if (event.isCanceled()) {
            ci.cancel();
            return original;
        }

        horizontalOffset.set(event.getHorizontalOffset());

        return !event.isCompact();
    }

    @ModifyArg(method = "renderEffects", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;"))
    private <E extends MobEffectInstance> Iterable<E> kilt$filterShouldRenderEffect(Iterable<E> elements) {
        return Streams.stream(elements).filter(ForgeHooksClient::shouldRenderEffect).toList();
    }

    @WrapWithCondition(method = "renderIcons", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private boolean kilt$disableBlitIfCustomRendered(PoseStack poseStack, int x, int y, int blitOffset, int width, int height, TextureAtlasSprite textureAtlasSprite, @Local MobEffectInstance effect, @Local(ordinal = 2) int i) {
        var renderer = IClientMobEffectExtensions.of(effect);

        return !renderer.renderInventoryIcon(effect, (EffectRenderingInventoryScreen<T>) (Object) this, poseStack, x, i, this.getBlitOffset());
    }

    @WrapWithCondition(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/network/chat/Component;FFI)I"))
    private boolean kilt$disableTextIfCustomRendered(Font instance, PoseStack poseStack, Component text, float x, float y, int color, @Share("shouldCancel") LocalBooleanRef shouldCancel, @Local MobEffectInstance effect, @Local(ordinal = 0, argsOnly = true) int renderX, @Local(ordinal = 2) int i) {
        var renderer = IClientMobEffectExtensions.of(effect);

        if (renderer.renderInventoryText(effect, (EffectRenderingInventoryScreen<T>) (Object) this, poseStack, renderX, i, this.getBlitOffset())) {
            shouldCancel.set(true);
            return false;
        }

        return true;
    }

    @WrapWithCondition(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/lang/String;FFI)I"))
    private boolean kilt$disableTextIfCustomRendered(Font instance, PoseStack poseStack, String text, float x, float y, int color, @Share("shouldCancel") LocalBooleanRef shouldCancel) {
        if (shouldCancel.get()) {
            shouldCancel.set(false);
            return false;
        }

        return true;
    }
}
