// TRACKED HASH: fd1859323d2b7b647915a5c458b0159a1f4e13b1
package xyz.bluspring.kilt.injects.client.gui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
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

import static net.neoforged.neoforge.client.gui.VanillaGuiLayers.*;

@Mixin(Gui.class)
public abstract class GuiInject implements GuiInjection {
    // Kilt TODO: *[SCREAMS]*

    @Shadow
    protected abstract void renderCameraOverlays(GuiGraphics guiGraphics, DeltaTracker deltaTracker);

    @Shadow
    @Final
    private LayeredDraw layers;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Unique private final GuiLayerManager layerManager = new GuiLayerManager();

    @Unique public int leftHeight;
    @Unique public int rightHeight;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$setupVanillaRenderLayers(Minecraft minecraft, CallbackInfo ci) {
        GuiLayerManager playerHealthComponents = new GuiLayerManager()
            .kilt$addVanilla(PLAYER_HEALTH)
            .kilt$addVanilla(ARMOR_LEVEL)
            .kilt$addVanilla(FOOD_LEVEL);

        GuiLayerManager main = new GuiLayerManager()
            .kilt$addVanilla(CAMERA_OVERLAYS)
            .kilt$addVanilla(CROSSHAIR)
            .kilt$addVanilla(HOTBAR)
            .kilt$addVanilla(JUMP_METER)
            .kilt$addVanilla(EXPERIENCE_BAR)
            .add(playerHealthComponents, () -> minecraft.gameMode.canHurtPlayer())
            .kilt$addVanilla(VEHICLE_HEALTH)
            .kilt$addVanilla(AIR_LEVEL)
            .kilt$addVanilla(SELECTED_ITEM_NAME)
            .kilt$addVanilla(SPECTATOR_TOOLTIP)
            .kilt$addVanilla(EXPERIENCE_LEVEL)
            .kilt$addVanilla(EFFECTS)
            .kilt$addVanilla(BOSS_OVERLAY);

        GuiLayerManager extras = new GuiLayerManager()
            .kilt$addVanilla(DEMO_OVERLAY)
            .kilt$addVanilla(DEBUG_OVERLAY)
            .kilt$addVanilla(SCOREBOARD_SIDEBAR)
            .kilt$addVanilla(OVERLAY_MESSAGE)
            .kilt$addVanilla(TITLE)
            .kilt$addVanilla(CHAT)
            .kilt$addVanilla(TAB_LIST)
            .kilt$addVanilla(SUBTITLE_OVERLAY)
            .kilt$addVanilla(SAVING_INDICATOR);

        this.layerManager
            .add(main, () -> !minecraft.options.hideGui)
            .kilt$addVanilla(SLEEP_OVERLAY)
            .add(extras, () -> !minecraft.options.hideGui);
    }

    @Unique
    private void kilt$tryRenderLayer(ResourceLocation layerId, GuiGraphics guiGraphics, DeltaTracker deltaTracker, Runnable renderCallback) {
        if (!this.layerManager.kilt$callPreRenderEvent(layerId, guiGraphics, deltaTracker)) {
            renderCallback.run();
            this.layerManager.kilt$callPostRenderEvent(layerId, guiGraphics, deltaTracker);
        }

        this.layerManager.kilt$renderFrom(layerId, guiGraphics, deltaTracker);
    }

    // Kilt: I stole all of this from Porting Lib, but not to worry, I wrote the damn code
    @WrapMethod(method = "method_55808")
    private void kilt$tryRenderBossOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(BOSS_OVERLAY, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderHotbar(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void kilt$tryRenderHotbar(SpectatorGui instance, GuiGraphics guiGraphics, Operation<Void> original, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        kilt$tryRenderLayer(HOTBAR, guiGraphics, deltaTracker, () -> original.call(instance, guiGraphics));
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void kilt$tryRenderHotbar(Gui instance, GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(HOTBAR, guiGraphics, deltaTracker, () -> original.call(instance, guiGraphics, deltaTracker));
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;jumpableVehicle()Lnet/minecraft/world/entity/PlayerRideableJumping;"))
    private void kilt$renderJumpAndExperienceLayers(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // TODO: this renders before jump meter and experience bar, but we can't wrap blocks of code.
        this.layerManager.kilt$renderFrom(JUMP_METER, guiGraphics, deltaTracker);
        this.layerManager.kilt$renderFrom(EXPERIENCE_BAR, guiGraphics, deltaTracker);
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderJumpMeter(Lnet/minecraft/world/entity/PlayerRideableJumping;Lnet/minecraft/client/gui/GuiGraphics;I)V"))
    private void kilt$tryRenderJumpBar(Gui instance, PlayerRideableJumping rideable, GuiGraphics guiGraphics, int x, Operation<Void> original, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        if (!this.layerManager.kilt$callPreRenderEvent(JUMP_METER, guiGraphics, deltaTracker)) {
            original.call(instance, rideable, guiGraphics, x);
            this.layerManager.kilt$callPostRenderEvent(JUMP_METER, guiGraphics, deltaTracker);
        }
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderExperienceBar(Lnet/minecraft/client/gui/GuiGraphics;I)V"))
    private void kilt$tryRenderExperienceBar(Gui instance, GuiGraphics guiGraphics, int x, Operation<Void> original, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        if (!this.layerManager.kilt$callPreRenderEvent(EXPERIENCE_BAR, guiGraphics, deltaTracker)) {
            original.call(instance, guiGraphics, x);
            this.layerManager.kilt$callPostRenderEvent(EXPERIENCE_BAR, guiGraphics, deltaTracker);
        }
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderPlayerHealth(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void kilt$tryRenderPlayerBar(Gui instance, GuiGraphics guiGraphics, Operation<Void> original, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        if (!this.layerManager.kilt$callPreRenderEvent(EXPERIENCE_BAR, guiGraphics, deltaTracker)) {
            original.call(instance, guiGraphics);
            this.layerManager.kilt$callPostRenderEvent(EXPERIENCE_BAR, guiGraphics, deltaTracker);
        }
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderPlayerHealth(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void kilt$tryRenderPlayerHealth(Gui instance, GuiGraphics guiGraphics, Operation<Void> original, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        if (!this.layerManager.kilt$callPreRenderEvent(PLAYER_HEALTH, guiGraphics, deltaTracker)) {
            original.call(instance, guiGraphics);
            this.layerManager.kilt$callPostRenderEvent(PLAYER_HEALTH, guiGraphics, deltaTracker);
        }
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVehicleHealth(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void kilt$renderPlayerHealthLayers(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        this.layerManager.kilt$renderFrom(PLAYER_HEALTH, guiGraphics, deltaTracker);
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVehicleHealth(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void kilt$tryRenderVehicleHealth(Gui instance, GuiGraphics guiGraphics, Operation<Void> original, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        kilt$tryRenderLayer(VEHICLE_HEALTH, guiGraphics, deltaTracker, () -> original.call(instance, guiGraphics));
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;", ordinal = 1))
    private void kilt$renderTooltipLayers(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // TODO: this renders before selected item name and spectator tooltip, but we can't wrap blocks of code.
        this.layerManager.kilt$renderFrom(SELECTED_ITEM_NAME, guiGraphics, deltaTracker);
        this.layerManager.kilt$renderFrom(SPECTATOR_TOOLTIP, guiGraphics, deltaTracker);
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void kilt$tryRenderSelectedItemName(Gui instance, GuiGraphics guiGraphics, Operation<Void> original, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        if (!this.layerManager.kilt$callPreRenderEvent(SELECTED_ITEM_NAME, guiGraphics, deltaTracker)) {
            original.call(instance, guiGraphics);
            this.layerManager.kilt$callPostRenderEvent(SELECTED_ITEM_NAME, guiGraphics, deltaTracker);
        }
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void kilt$tryRenderSpectatorTooltip(SpectatorGui instance, GuiGraphics guiGraphics, Operation<Void> original, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        if (!this.layerManager.kilt$callPreRenderEvent(SPECTATOR_TOOLTIP, guiGraphics, deltaTracker)) {
            original.call(instance, guiGraphics);
            this.layerManager.kilt$callPostRenderEvent(SPECTATOR_TOOLTIP, guiGraphics, deltaTracker);
        }
    }

    @WrapMethod(method = "renderPlayerHealth")
    private void kilt$tryRenderPlayerHealth(GuiGraphics guiGraphics, Operation<Void> original) {
        // TODO: try to split it apart
        if (!this.layerManager.kilt$callPreRenderEvent(PLAYER_HEALTH, guiGraphics, minecraft.getTimer())
            && !this.layerManager.kilt$callPreRenderEvent(ARMOR_LEVEL, guiGraphics, minecraft.getTimer())
            && !this.layerManager.kilt$callPreRenderEvent(FOOD_LEVEL, guiGraphics, minecraft.getTimer())
            && !this.layerManager.kilt$callPreRenderEvent(AIR_LEVEL, guiGraphics, minecraft.getTimer())
        ) {
            original.call(guiGraphics);
            this.layerManager.kilt$callPostRenderEvent(PLAYER_HEALTH, guiGraphics, minecraft.getTimer());
            this.layerManager.kilt$callPostRenderEvent(ARMOR_LEVEL, guiGraphics, minecraft.getTimer());
            this.layerManager.kilt$callPostRenderEvent(FOOD_LEVEL, guiGraphics, minecraft.getTimer());
            this.layerManager.kilt$callPostRenderEvent(AIR_LEVEL, guiGraphics, minecraft.getTimer());
        }

        this.layerManager.kilt$renderFrom(PLAYER_HEALTH, guiGraphics, minecraft.getTimer());
        this.layerManager.kilt$renderFrom(ARMOR_LEVEL, guiGraphics, minecraft.getTimer());
        this.layerManager.kilt$renderFrom(FOOD_LEVEL, guiGraphics, minecraft.getTimer());
        this.layerManager.kilt$renderFrom(AIR_LEVEL, guiGraphics, minecraft.getTimer());
    }

    // Main layers
    @WrapMethod(method = "renderCameraOverlays")
    private void kilt$tryRenderCameraOverlays(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(CAMERA_OVERLAYS, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapMethod(method = "renderCrosshair")
    private void kilt$tryRenderCrosshairs(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(CROSSHAIR, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapMethod(method = "renderExperienceLevel")
    private void kilt$tryRenderExperienceLevel(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(EXPERIENCE_LEVEL, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapMethod(method = "renderEffects")
    private void kilt$tryRenderEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(EFFECTS, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    // Additional layers
    @WrapMethod(method = "renderDemoOverlay")
    private void kilt$tryRenderDemoOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(DEMO_OVERLAY, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapMethod(method = "method_55807")
    private void kilt$tryRenderDebugOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(DEBUG_OVERLAY, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapMethod(method = "renderScoreboardSidebar")
    private void kilt$tryRenderSidebarOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(SCOREBOARD_SIDEBAR, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapMethod(method = "renderOverlayMessage")
    private void kilt$tryRenderOverlayMessage(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(OVERLAY_MESSAGE, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapMethod(method = "renderTitle")
    private void kilt$tryRenderTitle(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(TITLE, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapMethod(method = "renderChat")
    private void kilt$tryRenderChat(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(CHAT, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapMethod(method = "renderTabList")
    private void kilt$tryRenderTabList(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(TAB_LIST, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapMethod(method = "method_55806")
    private void kilt$tryRenderSubtitleOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(SUBTITLE_OVERLAY, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapMethod(method = "renderSavingIndicator")
    private void kilt$tryRenderSaving(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(SAVING_INDICATOR, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapMethod(method = "renderSleepOverlay")
    private void kilt$tryRenderSleepOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        kilt$tryRenderLayer(SLEEP_OVERLAY, guiGraphics, deltaTracker, () -> original.call(guiGraphics, deltaTracker));
    }

    @WrapMethod(method = "render")
    private void kilt$callGuiRenderEvents(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        this.leftHeight = 39;
        this.rightHeight = 39;

        if (NeoForge.EVENT_BUS.post(new RenderGuiEvent.Pre(guiGraphics, deltaTracker)).isCanceled()) {
            return;
        }

        // Kilt: We have to let this all render somehow, so... basically recreate the method from scratch, except do this too.
        this.layerManager.kilt$renderFrom((GuiLayerManager.NamedLayer) null, guiGraphics, minecraft.getTimer());
        original.call(guiGraphics, deltaTracker);

        NeoForge.EVENT_BUS.post(new RenderGuiEvent.Post(guiGraphics, deltaTracker));
    }

    // Kilt: now starts the actual Neo patches
    // TODO: .... probably do that later lmao

    @ApiStatus.Internal
    public void initModdedOverlays() {
        this.layerManager.initModdedLayers();
    }

    @Override
    public int getLayerCount() {
        return this.layerManager.getLayerCount();
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