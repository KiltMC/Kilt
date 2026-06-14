// TRACKED HASH: fd1859323d2b7b647915a5c458b0159a1f4e13b1
package xyz.bluspring.kilt.injects.client.gui;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.gui.GuiInjection;
import xyz.bluspring.kilt.workarounds.KiltGuiLayers;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.player.Player;

@Mixin(Gui.class)
public abstract class GuiInject implements GuiInjection {
    @Shadow @Final private LayeredDraw layers;
    @Shadow @Final private Minecraft minecraft;
    @Shadow public abstract void renderSavingIndicator(GuiGraphics guiGraphics, DeltaTracker deltaTracker);

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

        instance.kilt$getLayerManager()
            .kilt$addVanilla(VanillaGuiLayers.HOTBAR)
            .kilt$addVanilla(VanillaGuiLayers.JUMP_METER)
            .kilt$addVanilla(VanillaGuiLayers.EXPERIENCE_BAR);

        var playerHealthComponents = new GuiLayerManager();
        playerHealthComponents
            .kilt$addVanilla(VanillaGuiLayers.PLAYER_HEALTH)
            .kilt$addVanilla(VanillaGuiLayers.ARMOR_LEVEL)
            .kilt$addVanilla(VanillaGuiLayers.FOOD_LEVEL);

        instance.kilt$getLayerManager()
            .add(playerHealthComponents, () -> this.minecraft.gameMode.canHurtPlayer())
            .kilt$addVanilla(VanillaGuiLayers.VEHICLE_HEALTH)
            .kilt$addVanilla(VanillaGuiLayers.AIR_LEVEL)
            .kilt$addVanilla(VanillaGuiLayers.SELECTED_ITEM_NAME)
            .kilt$addVanilla(VanillaGuiLayers.SPECTATOR_TOOLTIP);

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

    // Kilt: now starts the actual Neo patches
    // TODO: .... probably do that later lmao

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
