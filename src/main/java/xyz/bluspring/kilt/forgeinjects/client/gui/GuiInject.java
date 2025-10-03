// TRACKED HASH: fd1859323d2b7b647915a5c458b0159a1f4e13b1
package xyz.bluspring.kilt.forgeinjects.client.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.client.KiltClient;
import xyz.bluspring.kilt.injections.client.gui.GuiInjection;

import java.util.EnumSet;
import java.util.List;

@Mixin(Gui.class)
public abstract class GuiInject implements GuiInjection {
    @Unique
    private ForgeGui kilt$getGui() {
        return KiltClient.forgeGui;
    }

    // The list of all vanilla GUI overlays that require setupOverlayRenderState
    @Unique private static final EnumSet<VanillaGuiOverlay> kilt$overlayRenderStates = EnumSet.complementOf(EnumSet.of(
        VanillaGuiOverlay.PLAYER_LIST,
        VanillaGuiOverlay.CHAT_PANEL,
        VanillaGuiOverlay.SCOREBOARD,
        VanillaGuiOverlay.SUBTITLES,
        VanillaGuiOverlay.TITLE_TEXT,
        VanillaGuiOverlay.RECORD_OVERLAY,
        VanillaGuiOverlay.FPS_GRAPH,
        VanillaGuiOverlay.DEBUG_TEXT,
        VanillaGuiOverlay.POTION_ICONS,
        VanillaGuiOverlay.SLEEP_FADE
    ));

    @Shadow public Minecraft minecraft;
    @Shadow public int screenWidth;
    @Shadow public int screenHeight;
    @Shadow public abstract void renderSelectedItemName(GuiGraphics guiGraphics);

    @WrapOperation(method = "renderEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;showIcon()Z"))
    private boolean kilt$checkIconVisible(MobEffectInstance instance, Operation<Boolean> original) {
        var renderer = IClientMobEffectExtensions.of(instance);
        return original.call(instance) && renderer.isVisibleInGui(instance);
    }

    @WrapWithCondition(method = "renderEffects", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean kilt$renderGuiIconWithCustomRenderer(List<Runnable> instance, Object e, @Local MobEffectInstance effectInstance, @Local GuiGraphics guiGraphics, @Local(ordinal = 0) int i, @Local(ordinal = 1) int j, @Local(ordinal = 0) float f) {
        var renderer = IClientMobEffectExtensions.of(effectInstance);

        return !renderer.renderGuiIcon(effectInstance, (Gui) (Object) this, guiGraphics, i, j, 0, f);
    }

    // This doesn't match what Forge is doing, but I'm rewriting it in mixins
    // for better Fabric mod support.
    @Unique
    private void renderAllOverlaysBetween(GuiGraphics guiGraphics, float delta, VanillaGuiOverlay start, VanillaGuiOverlay end) {
        var overlays = GuiOverlayManager.getOverlays();
        var window = this.minecraft.getWindow();

        var hasHitStart = false;
        for (NamedGuiOverlay overlay : overlays) {
            if (start == null || overlay.id().equals(start.id())) {
                hasHitStart = true;
                continue;
            }

            if (!hasHitStart)
                continue;

            if (overlay.id().equals(end.id()))
                break;

            try {
                ForgeGui.kilt$alreadyProcessedOverlays.add(overlay);

                if (pre(window, guiGraphics, delta, overlay))
                    continue;

                overlay.overlay().render(this.kilt$getGui(), guiGraphics, delta, this.screenWidth, this.screenHeight);

                post(guiGraphics, delta, overlay);
            } catch (Exception e) {
                Kilt.Companion.getLogger().error("Failed to render overlay " + overlay.id());
                e.printStackTrace();
            }
        }
    }

    private boolean pre(Window window, GuiGraphics guiGraphics, float delta, NamedGuiOverlay entry) {
        return MinecraftForge.EVENT_BUS.post(new RenderGuiOverlayEvent.Pre(window, guiGraphics, delta, entry));
    }

    private void post(GuiGraphics guiGraphics, float delta, VanillaGuiOverlay entry) {
        post(guiGraphics, delta, entry.type());
    }

    private void post(GuiGraphics guiGraphics, float delta, NamedGuiOverlay entry) {
        MinecraftForge.EVENT_BUS.post(new RenderGuiOverlayEvent.Post(this.minecraft.getWindow(), guiGraphics, delta, entry));
    }

    @Unique
    private boolean kilt$renderOverlay(GuiGraphics guiGraphics, float delta, VanillaGuiOverlay overlay, Operation<?> operation, Object... args) {
        return kilt$renderOverlayCheckPost(guiGraphics, delta, overlay, operation, true, args);
    }

    @Unique
    private boolean kilt$renderOverlayCheckPost(GuiGraphics guiGraphics, float delta, VanillaGuiOverlay overlay, Operation<?> operation, boolean shouldPost, Object... args) {
        var next = overlay.ordinal() == VanillaGuiOverlay.values().length - 1 ? null : VanillaGuiOverlay.values()[overlay.ordinal() + 1];
        renderAllOverlaysBetween(guiGraphics, delta, overlay, next);

        if (!pre(this.minecraft.getWindow(), guiGraphics, delta, overlay.type())) {
            if (kilt$overlayRenderStates.contains(overlay) && overlay.kilt$shouldSetupOverlayRenderState(this.kilt$getGui())) {
                // All of them seem to call this with blend and no depth testing, so we can
                // safely do this.
                this.kilt$getGui().setupOverlayRenderState(true, false);
            }

            operation.call(args);
            if (shouldPost)
                post(guiGraphics, delta, overlay);
            return true;
        }

        return false;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void kilt$resetForgeOverlayRenders(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        ForgeGui.kilt$alreadyProcessedOverlays.clear();
    }

    // Vignette
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVignette(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/Entity;)V"))
    public void kilt$renderVignette(Gui instance, GuiGraphics guiGraphics, Entity entity, Operation<Void> original, @Local(ordinal = 0) float delta) {
        kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.VIGNETTE, original, instance, guiGraphics, entity);
    }

    // Spyglass
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSpyglassOverlay(Lnet/minecraft/client/gui/GuiGraphics;F)V"))
    public void kilt$renderSpyglass(Gui instance, GuiGraphics guiGraphics, float scopeScale, Operation<Void> original, @Local(ordinal = 0) float delta) {
        kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.SPYGLASS, original, instance, guiGraphics, scopeScale);
    }

    // Helmet
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    public boolean kilt$renderHelmet(ItemStack instance, Item item, Operation<Boolean> original, @Local(argsOnly = true) GuiGraphics guiGraphics, @Local(ordinal = 0, argsOnly = true) float delta) {
        if (kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.HELMET, args -> null)) {
            if (this.kilt$getGui().kilt$renderHelmet(delta, guiGraphics, true)) {
                return original.call(instance, item);
            }

            post(guiGraphics, delta, VanillaGuiOverlay.HELMET);
        }

        return false;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTextureOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;F)V", ordinal = 0, shift = At.Shift.AFTER))
    private void kilt$postRenderHelmet(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.HELMET);
    }

    // Frostbite
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTextureOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;F)V"))
    public void kilt$renderFrostbite(Gui instance, GuiGraphics guiGraphics, ResourceLocation shaderLocation, float alpha, Operation<Void> original, @Local(ordinal = 0) float delta) {
        kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.FROSTBITE, original, instance, guiGraphics, shaderLocation, alpha);
    }

    // Portal
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"))
    public boolean kilt$renderPortal(LocalPlayer instance, MobEffect mobEffect, Operation<Boolean> original, @Local(argsOnly = true) GuiGraphics guiGraphics, @Local(ordinal = 0, argsOnly = true) float delta) {
        var overlay = VanillaGuiOverlay.PORTAL;
        var next = overlay.ordinal() == VanillaGuiOverlay.values().length - 1 ? null : VanillaGuiOverlay.values()[overlay.ordinal() + 1];
        renderAllOverlaysBetween(guiGraphics, delta, overlay, next);

        if (!pre(this.minecraft.getWindow(), guiGraphics, delta, overlay.type())) {
            if (kilt$overlayRenderStates.contains(overlay) && overlay.kilt$shouldSetupOverlayRenderState(this.kilt$getGui())) {
                // All of them seem to call this with blend and no depth testing, so we can
                // safely do this.
                this.kilt$getGui().setupOverlayRenderState(true, false);
            }

            boolean result = original.call(instance, mobEffect);
            post(guiGraphics, delta, overlay);
            return result;
        }

        return false;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;", shift = At.Shift.BEFORE))
    public void kilt$postRenderPortal(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.PORTAL);
    }

    // Hotbar
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHotbar(FLnet/minecraft/client/gui/GuiGraphics;)V"))
    public void kilt$renderSpectatorHotbar(Gui instance, float partialTick, GuiGraphics guiGraphics, Operation<Void> original, @Local(ordinal = 0) float delta) {
        kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.HOTBAR, original, instance, partialTick, guiGraphics);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderHotbar(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public void kilt$renderRegularHotbar(SpectatorGui instance, GuiGraphics guiGraphics, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) float delta) {
        kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.HOTBAR, original, instance, guiGraphics);
    }

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z", shift = At.Shift.BEFORE, ordinal = 1))
    public void kilt$postRenderHotbar(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.HOTBAR);
    }

    // Crosshair
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public void kilt$renderCrosshair(Gui instance, GuiGraphics guiGraphics, Operation<Void> original, @Local(ordinal = 0) float delta) {
        kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.CROSSHAIR, original, instance, guiGraphics);
    }

    // Boss Event Progress
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public void kilt$renderBossEventProgress(BossHealthOverlay instance, GuiGraphics guiGraphics, Operation<Void> original, @Local(ordinal = 0) float delta) {
        kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.BOSS_EVENT_PROGRESS, original, instance, guiGraphics);
    }

    // Player Health
    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHearts(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"))
    public void kilt$renderPlayerHealth(Gui instance, GuiGraphics guiGraphics, Player player, int x, int y, int height, int offsetHeartIndex, float maxHealth, int currentHealth, int displayHealth, int absorptionAmount, boolean renderHighlight, Operation<Void> original) {
        var delta = this.minecraft.getPartialTick();
        kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.PLAYER_HEALTH, original, instance, guiGraphics, player, x, y, height, offsetHeartIndex, maxHealth, currentHealth, displayHealth, absorptionAmount, renderHighlight);
    }

    @ModifyExpressionValue(method = "renderPlayerHealth", at =  @At(value = "CONSTANT", args = "intValue=10"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z", ordinal = 0), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHearts(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V")))
    private int kilt$renderPlayerArmor(int constant, GuiGraphics guiGraphics, @Share("armor") LocalBooleanRef result, @Share("hasRun") LocalBooleanRef hasRun) {
        if (hasRun.get())
            return constant;

        var delta = this.minecraft.getPartialTick();
        if (kilt$renderOverlayCheckPost(guiGraphics, delta, VanillaGuiOverlay.ARMOR_LEVEL, args -> null, false)) {
            result.set(true);
            hasRun.set(true);
            return constant;
        }
        result.set(false);
        hasRun.set(true);
        return 0;
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "CONSTANT", args = "stringValue=health"))
    private void kilt$postRenderPlayerArmor(GuiGraphics guiGraphics, CallbackInfo ci, @Share("armor") LocalBooleanRef result, @Share("hasRun") LocalBooleanRef hasRun) {
        if (result.get())
            post(guiGraphics, this.minecraft.getPartialTick(), VanillaGuiOverlay.ARMOR_LEVEL);

        hasRun.set(false);
    }

    @ModifyExpressionValue(method = "renderPlayerHealth", at =  @At(value = "CONSTANT", args = "intValue=10"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"), to = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMaxAirSupply()I")))
    private int kilt$renderPlayerFood(int constant, GuiGraphics guiGraphics, @Share("food") LocalBooleanRef result, @Share("hasRun") LocalBooleanRef hasRun) {
        if (hasRun.get())
            return constant;

        var delta = this.minecraft.getPartialTick();
        this.kilt$getGui().rightHeight += 10;
        if (kilt$renderOverlayCheckPost(guiGraphics, delta, VanillaGuiOverlay.FOOD_LEVEL, args -> null, false)) {
            result.set(true);
            hasRun.set(true);
            return constant;
        }
        result.set(false);
        hasRun.set(true);
        return 0;
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "CONSTANT", args = "stringValue=air"), cancellable = true)
    private void kilt$postRenderPlayerFoodAndRenderAir(GuiGraphics guiGraphics, CallbackInfo ci, @Share("food") LocalBooleanRef result, @Share("hasRun") LocalBooleanRef hasRun) {
        var delta = this.minecraft.getPartialTick();
        if (result.get())
            post(guiGraphics, this.minecraft.getPartialTick(), VanillaGuiOverlay.FOOD_LEVEL);
        this.kilt$getGui().rightHeight = 39;
        // End of the method so we can just cancel
        if (!kilt$renderOverlayCheckPost(guiGraphics, delta, VanillaGuiOverlay.AIR_LEVEL, args -> null, false))
            ci.cancel();

        hasRun.set(false);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVehicleHealth(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void kilt$renderMountHealth(Gui instance, GuiGraphics guiGraphics, Operation<Void> original, @Local(ordinal = 0) float delta) {
        kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.MOUNT_HEALTH, original, instance, guiGraphics);
    }

    // Jump Bar
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderJumpMeter(Lnet/minecraft/world/entity/PlayerRideableJumping;Lnet/minecraft/client/gui/GuiGraphics;I)V"))
    public void kilt$renderJumpBar(Gui instance, PlayerRideableJumping rideable, GuiGraphics guiGraphics, int x, Operation<Void> original, @Local(ordinal = 0) float delta) {
        kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.JUMP_BAR, original, instance, rideable, guiGraphics, x);
    }

    // Experience Bar
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderExperienceBar(Lnet/minecraft/client/gui/GuiGraphics;I)V"))
    public void kilt$renderExperienceBar(Gui instance, GuiGraphics guiGraphics, int x, Operation<Void> original, @Local(ordinal = 0) float delta) {
        kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.EXPERIENCE_BAR, original, instance, guiGraphics, x);
    }

    // Item Name
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public void kilt$renderItemName(Gui instance, GuiGraphics guiGraphics, Operation<Void> original, @Local(ordinal = 0) float partialTick) {
        kilt$renderOverlay(guiGraphics, partialTick, VanillaGuiOverlay.ITEM_NAME, original, instance, guiGraphics);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public void kilt$renderItemName(SpectatorGui instance, GuiGraphics guiGraphics, Operation<Void> original, @Local(ordinal = 0) float partialTick) {
        kilt$renderOverlay(guiGraphics, partialTick, VanillaGuiOverlay.ITEM_NAME, original, instance, guiGraphics);
    }

    // Sleep Fade
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I", ordinal = 0))
    public int kilt$renderSleepFade(LocalPlayer instance, Operation<Integer> original, @Local GuiGraphics guiGraphics, @Local(ordinal = 0) float partialTick, @Share("sleep_fade") LocalBooleanRef result) {
        if (kilt$renderOverlay(guiGraphics, partialTick, VanillaGuiOverlay.SLEEP_FADE, args -> null)) {
            result.set(false);
            return -1;
        }
        result.set(true);

        return original.call(instance);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isDemo()Z", shift = At.Shift.BEFORE))
    public void kilt$postRenderSleepFade(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci, @Share("sleep_fade") LocalBooleanRef result) {
        if (result.get())
            post(guiGraphics, partialTick, VanillaGuiOverlay.SLEEP_FADE);
    }

    @Unique private int kilt$yShift = 59;

    @Override
    public void renderSelectedItemName(GuiGraphics guiGraphics, int yShift) {
        kilt$yShift = yShift;
        this.renderSelectedItemName(guiGraphics);
        kilt$yShift = 59;
    }

    @ModifyExpressionValue(method = "renderSelectedItemName", at = @At(value = "CONSTANT", args = "intValue=59"))
    private int kilt$shiftYOfTooltip(int constant) {
        if (kilt$yShift != 59)
            return kilt$yShift;

        return constant;
    }

    // TODO: Debug text and FPS graph render, that should go into DebugScreenOverlay

    // TODO: Potion icons, record overlay, subtitles, title, scoreboard, chat, player list
}