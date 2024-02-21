package xyz.bluspring.kilt.forgeinjects.client.gui;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
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

import java.util.List;

@Mixin(Gui.class)
public abstract class GuiInject implements GuiInjection {
    private ForgeGui getGui() {
        return KiltClient.Companion.getForgeGui();
    }

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
    private boolean kilt$renderGuiIconWithCustomRenderer(List<Runnable> instance, Object e, @Local MobEffectInstance effectInstance, @Local GuiGraphics guiGraphics, @Local(name = "i") int i, @Local(name = "j") int j, @Local(name = "f") float f) {
        var renderer = IClientMobEffectExtensions.of(effectInstance);

        return !renderer.renderGuiIcon(effectInstance, (Gui) (Object) this, guiGraphics, i, j, 0, f);
    }

    // This doesn't match what Forge is doing, but I'm rewriting it in mixins
    // for better Fabric mod support.
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
                if (pre(window, guiGraphics, delta, overlay))
                    continue;

                overlay.overlay().render(this.getGui(), guiGraphics, delta, this.screenWidth, this.screenHeight);

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

    private boolean kilt$renderOverlay(GuiGraphics guiGraphics, float delta, VanillaGuiOverlay overlay) {
        var next = overlay.ordinal() == VanillaGuiOverlay.values().length - 1 ? null : VanillaGuiOverlay.values()[overlay.ordinal() + 1];
        renderAllOverlaysBetween(guiGraphics, delta, overlay, next);

        return !pre(this.minecraft.getWindow(), guiGraphics, delta, overlay.type());
    }

    // Vignette
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVignette(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/Entity;)V"))
    public boolean kilt$renderVignette(Gui instance, GuiGraphics guiGraphics, Entity entity, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.VIGNETTE);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getDeltaFrameTime()F", shift = At.Shift.BEFORE))
    public void kilt$postRenderVignette(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.VIGNETTE);
    }

    // Spyglass
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSpyglassOverlay(Lnet/minecraft/client/gui/GuiGraphics;F)V"))
    public boolean kilt$renderSpyglass(Gui instance, GuiGraphics guiGraphics, float scopeScale, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.SPYGLASS);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSpyglassOverlay(Lnet/minecraft/client/gui/GuiGraphics;F)V", shift = At.Shift.AFTER))
    public void kilt$postRenderSpyglass(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.SPYGLASS);
    }

    // Helmet
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    public boolean kilt$renderHelmet(ItemStack instance, Item item, @Local(argsOnly = true) GuiGraphics guiGraphics, @Local(ordinal = 0, index = 0, argsOnly = true) float delta) {
        if (kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.HELMET)) {
            // Let's just overwrite this with a call to this renderHelmet
            this.getGui().renderHelmet(delta, guiGraphics);

            post(guiGraphics, delta, VanillaGuiOverlay.HELMET);
        }

        return false;
    }

    // Frostbite
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTextureOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;F)V"))
    public boolean kilt$renderFrostbite(Gui instance, GuiGraphics guiGraphics, ResourceLocation shaderLocation, float alpha, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.FROSTBITE);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTextureOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;F)V", shift = At.Shift.AFTER))
    public void kilt$postRenderFrostbite(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.FROSTBITE);
    }

    // Portal
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"))
    public boolean kilt$renderPortal(LocalPlayer player, MobEffect effect, @Local(argsOnly = true) GuiGraphics guiGraphics, @Local(ordinal = 0, index = 0) float delta) {
        return !kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.PORTAL) && player.hasEffect(effect);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;", shift = At.Shift.BEFORE))
    public void kilt$postRenderPortal(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.PORTAL);
    }

    // Hotbar
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHotbar(FLnet/minecraft/client/gui/GuiGraphics;)V"))
    public boolean kilt$renderSpectatorHotbar(Gui instance, float partialTick, GuiGraphics guiGraphics, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.HOTBAR);
    }

    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z", ordinal = 0))
    public boolean kilt$renderRegularHotbar(Options instance, @Local GuiGraphics guiGraphics, @Local(ordinal = 0, index = 0) float delta) {
        return !kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.HOTBAR) && instance.hideGui;
    }

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z", shift = At.Shift.BEFORE, ordinal = 1))
    public void kilt$postRenderHotbar(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.HOTBAR);
    }

    // Crosshair
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public boolean kilt$renderCrosshair(Gui instance, GuiGraphics guiGraphics, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.CROSSHAIR);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V", shift = At.Shift.AFTER))
    public void kilt$postRenderCrosshair(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.CROSSHAIR);
    }

    // Boss Event Progress
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public boolean kilt$renderBossEventProgress(BossHealthOverlay instance, GuiGraphics guiGraphics, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.BOSS_EVENT_PROGRESS);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public void kilt$postRenderBossEventProgress(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.BOSS_EVENT_PROGRESS);
    }

    // Player Health
    @WrapWithCondition(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHearts(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"))
    public boolean kilt$renderPlayerHealth(Gui instance, GuiGraphics guiGraphics, Player player, int x, int y, int height, int offsetHeartIndex, float maxHealth, int currentHealth, int displayHealth, int absorptionAmount, boolean renderHighlight) {
        var delta = this.minecraft.getPartialTick();
        return kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.PLAYER_HEALTH);
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHearts(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V", shift = At.Shift.AFTER))
    public void kilt$postRenderPlayerHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
        var delta = this.minecraft.getPartialTick();
        post(guiGraphics, delta, VanillaGuiOverlay.PLAYER_HEALTH);

        // TODO: figure out how to wrap blocks of code with conditional checks
        if (!kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.ARMOR_LEVEL)) {
            post(guiGraphics, delta, VanillaGuiOverlay.ARMOR_LEVEL);
        }

        if (!kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.FOOD_LEVEL)) {
            post(guiGraphics, delta, VanillaGuiOverlay.FOOD_LEVEL);
        }

        if (!kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.MOUNT_HEALTH)) {
            post(guiGraphics, delta, VanillaGuiOverlay.MOUNT_HEALTH);
        }

        if (!kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.AIR_LEVEL)) {
            post(guiGraphics, delta, VanillaGuiOverlay.AIR_LEVEL);
        }
    }


    // Jump Bar
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderJumpMeter(Lnet/minecraft/world/entity/PlayerRideableJumping;Lnet/minecraft/client/gui/GuiGraphics;I)V"))
    public boolean kilt$renderJumpBar(Gui instance, PlayerRideableJumping rideable, GuiGraphics guiGraphics, int x, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.JUMP_BAR);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderJumpMeter(Lnet/minecraft/world/entity/PlayerRideableJumping;Lnet/minecraft/client/gui/GuiGraphics;I)V", shift = At.Shift.AFTER))
    public void kilt$postRenderJumpBar(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.JUMP_BAR);
    }

    // Experience Bar
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderExperienceBar(Lnet/minecraft/client/gui/GuiGraphics;I)V"))
    public boolean kilt$renderExperienceBar(Gui instance, GuiGraphics guiGraphics, int x, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(guiGraphics, delta, VanillaGuiOverlay.EXPERIENCE_BAR);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderExperienceBar(Lnet/minecraft/client/gui/GuiGraphics;I)V", shift = At.Shift.AFTER))
    public void kilt$postRenderExperienceBar(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.EXPERIENCE_BAR);
    }

    // Item Name
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public boolean kilt$renderItemName(SpectatorGui instance, GuiGraphics guiGraphics, @Local(ordinal = 0, index = 0) float partialTick) {
        return kilt$renderOverlay(guiGraphics, partialTick, VanillaGuiOverlay.ITEM_NAME);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I", ordinal = 0, shift = At.Shift.BEFORE))
    public void kilt$postRenderItemName(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.ITEM_NAME);
    }

    // Sleep Fade
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I", ordinal = 0))
    public int kilt$renderSleepFade(LocalPlayer instance, Operation<Integer> original, @Local GuiGraphics guiGraphics, @Local(ordinal = 0, index = 0) float partialTick) {
        if (kilt$renderOverlay(guiGraphics, partialTick, VanillaGuiOverlay.SLEEP_FADE))
            return -1;

        return original.call(instance);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isDemo()Z", shift = At.Shift.BEFORE))
    public void kilt$postRenderSleepFade(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        post(guiGraphics, partialTick, VanillaGuiOverlay.SLEEP_FADE);
    }

    @Unique private int kilt$yShift = 59;

    @Override
    public void renderSelectedItemName(GuiGraphics guiGraphics, int yShift) {
        kilt$yShift = yShift;
        this.renderSelectedItemName(guiGraphics);
        kilt$yShift = 59;
    }

    @ModifyConstant(method = "renderSelectedItemName", constant = @Constant(intValue = 59))
    private int kilt$shiftYOfTooltip(int constant) {
        return kilt$yShift;
    }

    // TODO: Debug text and FPS graph render, that should go into DebugScreenOverlay

    // TODO: Potion icons, record overlay, subtitles, title, scoreboard, chat, player list
}
