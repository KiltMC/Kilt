// TRACKED HASH: fd1859323d2b7b647915a5c458b0159a1f4e13b1
package xyz.bluspring.kilt.injects.client.gui;

import java.util.Iterator;
import java.util.List;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.gui.GuiInjection;
import xyz.bluspring.kilt.util.IteratorWrapper;
import xyz.bluspring.kilt.workarounds.KiltGuiLayers;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(Gui.class)
public abstract class GuiInject implements GuiInjection {
    @Shadow @Final private LayeredDraw layers;
    @Shadow @Final private Minecraft minecraft;
    @Shadow public abstract void renderSavingIndicator(GuiGraphics guiGraphics, DeltaTracker deltaTracker);
    @Shadow @Final private ChatComponent chat;
    @Shadow protected abstract void renderSelectedItemName(GuiGraphics guiGraphics);
    @Shadow private ItemStack lastToolHighlight;

    @Unique private GuiLayerManager layerManager;

    @Unique public int leftHeight;
    @Unique public int rightHeight;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;<init>(Lnet/minecraft/client/Minecraft;)V"))
    private void kilt$storeLayerManager(Minecraft minecraft, CallbackInfo ci) {
        this.layerManager = this.layers.kilt$getLayerManager();
    }

    @Definition(id = "renderCameraOverlays", method = "Lnet/minecraft/client/gui/Gui;renderCameraOverlays(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Expression("?.add(this::renderCameraOverlays)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedCameraOverlays(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.CAMERA_OVERLAYS, layer);
        return original.call(instance, layer);
    }

    @Definition(id = "renderCrosshair", method = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Expression("?.add(this::renderCrosshair)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedCrosshair(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.CROSSHAIR, layer);
        return original.call(instance, layer);
    }

    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Definition(id = "renderHotbarAndDecorations", method = "Lnet/minecraft/client/gui/Gui;renderHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Expression("?.add(this::renderHotbarAndDecorations)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedHotbarAndDecorations(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(KiltGuiLayers.HOTBAR_AND_DECORATIONS, layer);

        instance.kilt$add(VanillaGuiLayers.HOTBAR);
        instance.kilt$add(VanillaGuiLayers.JUMP_METER);
        instance.kilt$add(VanillaGuiLayers.EXPERIENCE_BAR);

        var playerHealthComponents = new LayeredDraw();
        playerHealthComponents.kilt$add(VanillaGuiLayers.PLAYER_HEALTH);
        playerHealthComponents.kilt$add(VanillaGuiLayers.ARMOR_LEVEL);
        playerHealthComponents.kilt$add(VanillaGuiLayers.FOOD_LEVEL);

        instance.add(playerHealthComponents, () -> this.minecraft.gameMode.canHurtPlayer())
            .kilt$add(VanillaGuiLayers.VEHICLE_HEALTH);
        instance.kilt$add(VanillaGuiLayers.AIR_LEVEL);
        instance.kilt$add(VanillaGuiLayers.SELECTED_ITEM_NAME);
        instance.kilt$add(VanillaGuiLayers.SPECTATOR_TOOLTIP);

        return original.call(instance, layer);
    }

    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Definition(id = "renderExperienceLevel", method = "Lnet/minecraft/client/gui/Gui;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Expression("?.add(this::renderExperienceLevel)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedExperienceLevel(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.EXPERIENCE_LEVEL, layer);
        return original.call(instance, layer);
    }

    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Definition(id = "renderEffects", method = "Lnet/minecraft/client/gui/Gui;renderEffects(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Expression("?.add(this::renderEffects)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedEffects(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.EFFECTS, layer);
        return original.call(instance, layer);
    }

    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Definition(id = "method_55808", method = "Lnet/minecraft/client/gui/Gui;method_55808(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Expression("?.add(this::method_55808)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedBossOverlay(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.BOSS_OVERLAY, layer);
        return original.call(instance, layer);
    }

    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Definition(id = "renderDemoOverlay", method = "Lnet/minecraft/client/gui/Gui;renderDemoOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Expression("?.add(this::renderDemoOverlay)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedDemoOverlay(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.DEMO_OVERLAY, layer);
        return original.call(instance, layer);
    }

    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Definition(id = "method_55807", method = "Lnet/minecraft/client/gui/Gui;method_55807(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Expression("?.add(this::method_55807)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedDebugOverlay(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.DEBUG_OVERLAY, layer);
        return original.call(instance, layer);
    }

    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Definition(id = "renderScoreboardSidebar", method = "Lnet/minecraft/client/gui/Gui;renderScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Expression("?.add(this::renderScoreboardSidebar)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedScoreboardSidebar(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.SCOREBOARD_SIDEBAR, layer);
        return original.call(instance, layer);
    }

    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Definition(id = "renderOverlayMessage", method = "Lnet/minecraft/client/gui/Gui;renderOverlayMessage(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Expression("?.add(this::renderOverlayMessage)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedOverlayMessage(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.OVERLAY_MESSAGE, layer);
        return original.call(instance, layer);
    }

    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Definition(id = "renderTitle", method = "Lnet/minecraft/client/gui/Gui;renderTitle(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Expression("?.add(this::renderTitle)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedTitle(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.TITLE, layer);
        return original.call(instance, layer);
    }

    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Definition(id = "renderChat", method = "Lnet/minecraft/client/gui/Gui;renderChat(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Expression("?.add(this::renderChat)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedChat(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.CHAT, layer);
        return original.call(instance, layer);
    }

    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Definition(id = "renderTabList", method = "Lnet/minecraft/client/gui/Gui;renderTabList(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Expression("?.add(this::renderTabList)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedTabList(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.TAB_LIST, layer);
        return original.call(instance, layer);
    }

    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Definition(id = "method_55806", method = "Lnet/minecraft/client/gui/Gui;method_55806(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Expression("?.add(this::method_55806)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedSubtitleOverlay(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.SUBTITLE_OVERLAY, layer);
        instance.kilt$getLayerManager().add(VanillaGuiLayers.SAVING_INDICATOR, this::renderSavingIndicator); // Neo renders this twice... time to retain that behaviour!
        return original.call(instance, layer);
    }

    @Definition(id = "add", method = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    @Definition(id = "renderSleepOverlay", method = "Lnet/minecraft/client/gui/Gui;renderSleepOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
    @Expression("?.add(this::renderSleepOverlay)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private LayeredDraw kilt$namedSleepOverlay(LayeredDraw instance, LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        instance.kilt$addVanilla(VanillaGuiLayers.SLEEP_OVERLAY, layer);
        return original.call(instance, layer);
    }

    @WrapMethod(method = "render")
    private void kilt$callGuiRenderEvents(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        this.leftHeight = 39;
        this.rightHeight = 39;

        if (NeoForge.EVENT_BUS.post(new RenderGuiEvent.Pre(guiGraphics, deltaTracker)).isCanceled()) {
            return;
        }

        original.call(guiGraphics, deltaTracker);

        NeoForge.EVENT_BUS.post(new RenderGuiEvent.Post(guiGraphics, deltaTracker));
    }

    @Definition(id = "itemStack", local = @Local(type = ItemStack.class))
    @Definition(id = "is", method = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    @Definition(id = "CARVED_PUMPKIN", field = "Lnet/minecraft/world/level/block/Blocks;CARVED_PUMPKIN:Lnet/minecraft/world/level/block/Block;")
    @Definition(id = "asItem", method = "Lnet/minecraft/world/level/block/Block;asItem()Lnet/minecraft/world/item/Item;")
    @Expression("itemStack.is(CARVED_PUMPKIN.asItem())")
    @WrapOperation(method = "renderCameraOverlays", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$tryRenderHelmetOverlay(ItemStack instance, Item item, Operation<Boolean> original, @Local(argsOnly = true) GuiGraphics guiGraphics, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        var originalValue = original.call(instance, item);

        if (!originalValue) {
            IClientItemExtensions.of(instance).renderHelmetOverlay(instance, this.minecraft.player, guiGraphics, deltaTracker);
        }

        return originalValue;
    }

    @Definition(id = "guiGraphics", local = @Local(type = GuiGraphics.class, argsOnly = true))
    @Definition(id = "guiHeight", method = "Lnet/minecraft/client/gui/GuiGraphics;guiHeight()I")
    @Expression("guiGraphics.guiHeight() - @(68)")
    @ModifyExpressionValue(method = "renderOverlayMessage", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int kilt$tryHandleYShift(int original) {
        int yShift = Math.max(this.leftHeight, this.rightHeight) + (original - 59);
        return Math.max(yShift, original);
    }

    @WrapMethod(method = "renderChat")
    private void kilt$wrapWithGuiEvent(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (!this.chat.isChatFocused()) {
            var chatBottomMargin = 40;
            var event = NeoForge.EVENT_BUS.post(new CustomizeGuiOverlayEvent.Chat(this.minecraft.getWindow(), guiGraphics, deltaTracker, 0, guiGraphics.guiHeight() - chatBottomMargin));

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(event.getPosX(), (event.getPosY() - guiGraphics.guiHeight() + chatBottomMargin) / this.chat.getScale(), 0f);
            original.call(guiGraphics, deltaTracker);
            guiGraphics.pose().popPose();
        } else {
            original.call(guiGraphics, deltaTracker);
        }
    }

    @ModifyExpressionValue(method = "renderEffects", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0))
    private <E extends MobEffectInstance> Iterator<E> kilt$wrapWithVisibleEffectCheck(Iterator<E> original) {
        return new IteratorWrapper<>(original, effect -> {
            var renderer = IClientMobEffectExtensions.of(effect);
            if (!renderer.isVisibleInGui(effect))
                return null;

            return effect;
        });
    }

    @Definition(id = "list", local = @Local(type = List.class))
    @Definition(id = "add", method = "Ljava/util/List;add(Ljava/lang/Object;)Z")
    @Definition(id = "method_18620", method = "Lnet/minecraft/client/gui/Gui;method_18620(Lnet/minecraft/client/gui/GuiGraphics;FIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V")
    @Expression("list.add(::method_18620)")
    @WrapOperation(method = "renderEffects", at = @At("MIXINEXTRAS:EXPRESSION"))
    private <E> boolean kilt$checkShouldRenderGuiIcon(List<E> instance, E e, Operation<Boolean> original, @Local MobEffectInstance effect, @Local(argsOnly = true) GuiGraphics guiGraphics, @Local(ordinal = 2) int x, @Local(ordinal = 3) int y, @Local(ordinal = 0) float alpha) {
        var renderer = IClientMobEffectExtensions.of(effect);
        if (renderer.renderGuiIcon(effect, (Gui) (Object) this, guiGraphics, x, y, 0, alpha))
            return false;

        return original.call(instance, e);
    }

    @Unique private int kilt$yShift = Integer.MIN_VALUE;

    @Override
    public void renderSelectedItemName(GuiGraphics graphics, int yShift) {
        this.kilt$yShift = yShift;
        this.renderSelectedItemName(graphics);
        this.kilt$yShift = 0;
    }

    @WrapOperation(method = "renderSelectedItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 0))
    private MutableComponent kilt$tryUseStyleModifier(MutableComponent instance, ChatFormatting format, Operation<MutableComponent> original) {
        if (this.lastToolHighlight.getRarity().kilt$hasCustomStyleModifier()) {
            return instance.withStyle(this.lastToolHighlight.getRarity().getStyleModifier());
        }

        return original.call(instance, format);
    }

    @ModifyVariable(method = "renderSelectedItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I", ordinal = 0))
    private MutableComponent kilt$wrapWithHighlightTip(MutableComponent original) {
        return this.lastToolHighlight.getHighlightTip(original).copy();
    }

    @ModifyExpressionValue(method = "renderSelectedItemName", at = @At(value = "CONSTANT", args = "intValue=59"))
    private int kilt$tryUseYShift(int original) {
        if (this.kilt$yShift != Integer.MIN_VALUE) {
            return Math.max(this.kilt$yShift, original);
        }

        return Math.max(Math.max(this.leftHeight, this.rightHeight), original);
    }

    @WrapOperation(method = "renderSelectedItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)I"))
    private int kilt$tryRenderWithCustomFont(GuiGraphics instance, Font font, Component text, int x, int y, int xOffset, int color, Operation<Integer> original) {
        var kiltFont = IClientItemExtensions.of(this.lastToolHighlight)
            .getFont(this.lastToolHighlight, IClientItemExtensions.FontContext.SELECTED_ITEM_NAME);

        if (kiltFont == null) {
            return original.call(instance, font, text, x, y, xOffset, color);
        } else {
            x = (instance.guiWidth() - kiltFont.width(text)) / 2;
            return original.call(instance, kiltFont, text, x, y, xOffset, color);
        }
    }

    // Kilt: ... why implement demo timer hide?

    @ModifyExpressionValue(method = "renderPlayerHealth", at = @At(value = "CONSTANT", args = "intValue=39"))
    private int kilt$tryShiftByLeftHeight(int original) {
        int diff = 39 - original;
        return this.leftHeight + diff;
    }

    @Definition(id = "r", local = @Local(type = int.class, ordinal = 8))
    @Definition(id = "n", local = @Local(type = int.class, ordinal = 4))
    @Expression("r = n - 10")
    @Inject(method = "renderPlayerHealth", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void kilt$shiftLeftHeight(GuiGraphics guiGraphics, CallbackInfo ci, @Local(ordinal = 6) int p, @Local(ordinal = 7) int q) {
        this.leftHeight += (p - 1) * q + 10;
    }

    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderArmor(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIII)V"))
    private void kilt$tryRenderArmorWithLeftHeight(GuiGraphics guiGraphics, Player player, int y, int heartRows, int height, int x, Operation<Void> original, @Local(ordinal = 4) int originalYOffset) {
        int diff = originalYOffset - y;
        original.call(guiGraphics, player, diff + y, heartRows, height, x);
        if (player.getArmorValue() > 0) {
            this.leftHeight += 10;
        }
    }

    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V"))
    private void kilt$tryRenderFoodWithRightHeight(Gui instance, GuiGraphics guiGraphics, Player player, int y, int x, Operation<Void> original) {
        original.call(instance, guiGraphics, player, y, x);
        this.rightHeight += 10;
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V", shift = At.Shift.AFTER))
    private void kilt$incrementRightHeightAfterAir(GuiGraphics guiGraphics, CallbackInfo ci) {
        this.rightHeight += 10;
    }

    @ModifyExpressionValue(method = "renderVehicleHealth", at = @At(value = "CONSTANT", args = "intValue=39"))
    private int kilt$tryRenderVehicleHealthWithRightHeight(int original) {
        int diff = 39 - original;
        return this.rightHeight + diff;
    }

    @Inject(method = "renderVehicleHealth", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I")) // Kilt: this isn't the right injection point, but close enough.
    private void kilt$incrementRightHeight(GuiGraphics guiGraphics, CallbackInfo ci) {
        this.rightHeight += 10;
    }

    @ModifyExpressionValue(method = "renderSpyglassOverlay", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Gui;SPYGLASS_SCOPE_LOCATION:Lnet/minecraft/resources/ResourceLocation;", opcode = Opcodes.GETSTATIC))
    private ResourceLocation kilt$tryUseScopeOverlay(ResourceLocation original) {
        var useItem = this.minecraft.player.getUseItem();
        var extensions = IClientItemExtensions.of(useItem);
        if (extensions != IClientItemExtensions.DEFAULT) {
            return extensions.getScopeOverlayTexture(useItem);
        }

        return original;
    }

    @Definition(id = "itemStack", local = @Local(type = ItemStack.class))
    @Definition(id = "getHoverName", method = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;")
    @Definition(id = "equals", method = "Lnet/minecraft/network/chat/Component;equals(Ljava/lang/Object;)Z")
    @Definition(id = "lastToolHighlight", field = "Lnet/minecraft/client/gui/Gui;lastToolHighlight:Lnet/minecraft/world/item/ItemStack;")
    @Expression("itemStack.getHoverName().equals(this.lastToolHighlight.getHoverName()) == false")
    @ModifyExpressionValue(method = "tick()V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$tryCheckHighlightTip(boolean original, @Local ItemStack stack) {
        return original || !stack.getHighlightTip(stack.getHoverName()).equals(this.lastToolHighlight.getHighlightTip(this.lastToolHighlight.getHoverName()));
    }

    @ApiStatus.Internal
    public void initModdedOverlays() {
        this.layerManager.initModdedLayers();
        this.layers.kilt$updateInternalLayers();
    }

    @Override
    public int getLayerCount() {
        return Math.max(this.layerManager.getLayerCount(), this.layers.kilt$getLayerCount());
    }

    @Mixin(Gui.HeartType.class)
    public abstract static class HeartTypeInject implements IExtensibleEnum {
        @ModifyReturnValue(method = "forPlayer", at = @At("RETURN"))
        private static Gui.HeartType kilt$firePlayerHeartTypeEvent(Gui.HeartType original, @Local(argsOnly = true) Player player) {
            return EventHooks.firePlayerHeartTypeEvent(player, original);
        }

        @CreateStatic
        private static ExtensionInfo getExtensionInfo() {
            return ExtensionInfo.nonExtended(Gui.HeartType.class);
        }
    }
}
