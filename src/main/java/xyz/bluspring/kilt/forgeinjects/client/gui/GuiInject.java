package xyz.bluspring.kilt.forgeinjects.client.gui;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.client.KiltClient;

@Mixin(Gui.class)
public class GuiInject {
    private ForgeGui getGui() {
        return KiltClient.Companion.getForgeGui();
    }

    @Shadow public Minecraft minecraft;

    @Shadow public int screenWidth;

    @Shadow public int screenHeight;

    // This doesn't match what Forge is doing, but I'm rewriting it in mixins
    // for better Fabric mod support.
    private void renderAllOverlaysBetween(PoseStack stack, float delta, VanillaGuiOverlay start, VanillaGuiOverlay end) {
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
                if (pre(window, stack, delta, overlay))
                    continue;

                overlay.overlay().render(this.getGui(), stack, delta, this.screenWidth, this.screenHeight);

                post(stack, delta, overlay);
            } catch (Exception e) {
                Kilt.Companion.getLogger().error("Failed to render overlay " + overlay.id());
                e.printStackTrace();
            }
        }
    }

    private boolean pre(Window window, PoseStack poseStack, float delta, NamedGuiOverlay entry) {
        return MinecraftForge.EVENT_BUS.post(new RenderGuiOverlayEvent.Pre(window, poseStack, delta, entry));
    }

    private void post(PoseStack poseStack, float delta, VanillaGuiOverlay entry) {
        post(poseStack, delta, entry.type());
    }

    private void post(PoseStack poseStack, float delta, NamedGuiOverlay entry) {
        MinecraftForge.EVENT_BUS.post(new RenderGuiOverlayEvent.Post(this.minecraft.getWindow(), poseStack, delta, entry));
    }

    private boolean kilt$renderOverlay(PoseStack poseStack, float delta, VanillaGuiOverlay overlay) {
        var next = overlay.ordinal() == VanillaGuiOverlay.values().length - 1 ? null : VanillaGuiOverlay.values()[overlay.ordinal() + 1];
        renderAllOverlaysBetween(poseStack, delta, overlay, next);

        return !pre(this.minecraft.getWindow(), poseStack, delta, overlay.type());
    }

    // Vignette
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVignette(Lnet/minecraft/world/entity/Entity;)V"))
    public boolean kilt$renderVignette(Gui instance, Entity entity, @Local PoseStack stack, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(stack, delta, VanillaGuiOverlay.VIGNETTE);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getDeltaFrameTime()F", shift = At.Shift.BEFORE))
    public void kilt$postRenderVignette(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        post(poseStack, partialTick, VanillaGuiOverlay.VIGNETTE);
    }

    // Spyglass
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSpyglassOverlay(F)V"))
    public boolean kilt$renderSpyglass(Gui gui, float scale, @Local PoseStack poseStack, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(poseStack, delta, VanillaGuiOverlay.SPYGLASS);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSpyglassOverlay(F)V", shift = At.Shift.AFTER))
    public void kilt$postRenderSpyglass(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        post(poseStack, partialTick, VanillaGuiOverlay.SPYGLASS);
    }

    // Helmet
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    public boolean kilt$renderHelmet(ItemStack instance, Item item, @Local PoseStack poseStack, @Local(ordinal = 0, index = 0) float delta) {
        if (kilt$renderOverlay(poseStack, delta, VanillaGuiOverlay.HELMET)) {
            // Let's just overwrite this with a call to this renderHelmet
            this.getGui().renderHelmet(delta, poseStack);

            post(poseStack, delta, VanillaGuiOverlay.HELMET);
        }

        return false;
    }

    // Frostbite
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTextureOverlay(Lnet/minecraft/resources/ResourceLocation;F)V"))
    public boolean kilt$renderFrostbite(Gui instance, ResourceLocation loc, float delta2, @Local PoseStack poseStack, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(poseStack, delta, VanillaGuiOverlay.FROSTBITE);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTextureOverlay(Lnet/minecraft/resources/ResourceLocation;F)V", shift = At.Shift.AFTER))
    public void kilt$postRenderFrostbite(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        post(poseStack, partialTick, VanillaGuiOverlay.FROSTBITE);
    }

    // Portal
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"))
    public boolean kilt$renderPortal(LocalPlayer player, MobEffect effect, @Local PoseStack poseStack, @Local(ordinal = 0, index = 0) float delta) {
        return !kilt$renderOverlay(poseStack, delta, VanillaGuiOverlay.PORTAL) && player.hasEffect(effect);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;", shift = At.Shift.BEFORE))
    public void kilt$postRenderPortal(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        post(poseStack, partialTick, VanillaGuiOverlay.PORTAL);
    }

    // Hotbar
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderHotbar(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    public boolean kilt$renderSpectatorHotbar(SpectatorGui gui, PoseStack poseStack, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(poseStack, delta, VanillaGuiOverlay.HOTBAR);
    }

    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z", ordinal = 0))
    public boolean kilt$renderRegularHotbar(Options instance, @Local PoseStack stack, @Local(ordinal = 0, index = 0) float delta) {
        return !kilt$renderOverlay(stack, delta, VanillaGuiOverlay.HOTBAR) && instance.hideGui;
    }

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z", shift = At.Shift.BEFORE, ordinal = 1))
    public void kilt$postRenderHotbar(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        post(poseStack, partialTick, VanillaGuiOverlay.HOTBAR);
    }

    // Crosshair
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    public boolean kilt$renderCrosshair(Gui instance, PoseStack stack, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(stack, delta, VanillaGuiOverlay.CROSSHAIR);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    public void kilt$postRenderCrosshair(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        post(poseStack, partialTick, VanillaGuiOverlay.CROSSHAIR);
    }

    // Boss Event Progress
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;render(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    public boolean kilt$renderBossEventProgress(BossHealthOverlay instance, PoseStack stack, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(stack, delta, VanillaGuiOverlay.BOSS_EVENT_PROGRESS);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;render(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    public void kilt$postRenderBossEventProgress(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        post(poseStack, partialTick, VanillaGuiOverlay.BOSS_EVENT_PROGRESS);
    }

    // Player Health
    @WrapWithCondition(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHearts(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"))
    public boolean kilt$renderPlayerHealth(Gui instance, PoseStack stack, Player player, int x, int y, int height, int i, float f, int j, int k, int l, boolean bl) {
        var delta = this.minecraft.getPartialTick();
        return kilt$renderOverlay(stack, delta, VanillaGuiOverlay.PLAYER_HEALTH);
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHearts(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V", shift = At.Shift.AFTER))
    public void kilt$postRenderPlayerHealth(PoseStack poseStack, CallbackInfo ci) {
        var delta = this.minecraft.getPartialTick();
        post(poseStack, delta, VanillaGuiOverlay.PLAYER_HEALTH);

        // TODO: figure out how to wrap blocks of code with conditional checks
        if (!kilt$renderOverlay(poseStack, delta, VanillaGuiOverlay.ARMOR_LEVEL)) {
            post(poseStack, delta, VanillaGuiOverlay.ARMOR_LEVEL);
        }

        if (!kilt$renderOverlay(poseStack, delta, VanillaGuiOverlay.FOOD_LEVEL)) {
            post(poseStack, delta, VanillaGuiOverlay.FOOD_LEVEL);
        }

        if (!kilt$renderOverlay(poseStack, delta, VanillaGuiOverlay.MOUNT_HEALTH)) {
            post(poseStack, delta, VanillaGuiOverlay.MOUNT_HEALTH);
        }

        if (!kilt$renderOverlay(poseStack, delta, VanillaGuiOverlay.AIR_LEVEL)) {
            post(poseStack, delta, VanillaGuiOverlay.AIR_LEVEL);
        }
    }


    // Jump Bar
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderJumpMeter(Lcom/mojang/blaze3d/vertex/PoseStack;I)V"))
    public boolean kilt$renderJumpBar(Gui instance, PoseStack stack, int i, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(stack, delta, VanillaGuiOverlay.JUMP_BAR);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderJumpMeter(Lcom/mojang/blaze3d/vertex/PoseStack;I)V", shift = At.Shift.AFTER))
    public void kilt$postRenderJumpBar(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        post(poseStack, partialTick, VanillaGuiOverlay.JUMP_BAR);
    }

    // Experience Bar
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderExperienceBar(Lcom/mojang/blaze3d/vertex/PoseStack;I)V"))
    public boolean kilt$renderExperienceBar(Gui instance, PoseStack poseStack, int i, @Local(ordinal = 0, index = 0) float delta) {
        return kilt$renderOverlay(poseStack, delta, VanillaGuiOverlay.EXPERIENCE_BAR);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderExperienceBar(Lcom/mojang/blaze3d/vertex/PoseStack;I)V", shift = At.Shift.AFTER))
    public void kilt$postRenderExperienceBar(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        post(poseStack, partialTick, VanillaGuiOverlay.EXPERIENCE_BAR);
    }

    // Item Name
    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;heldItemTooltips:Z"))
    public boolean kilt$renderItemName(Options instance, @Local PoseStack poseStack, @Local(ordinal = 0, index = 0) float partialTick) {
        return kilt$renderOverlay(poseStack, partialTick, VanillaGuiOverlay.ITEM_NAME) && instance.heldItemTooltips;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I", ordinal = 0, shift = At.Shift.BEFORE))
    public void kilt$postRenderItemName(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        post(poseStack, partialTick, VanillaGuiOverlay.ITEM_NAME);
    }

    // Sleep Fade
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I", ordinal = 0))
    public int kilt$renderSleepFade(LocalPlayer player, @Local PoseStack poseStack, @Local(ordinal = 0, index = 0) float partialTick) {
        if (kilt$renderOverlay(poseStack, partialTick, VanillaGuiOverlay.SLEEP_FADE))
            return -1;

        return player.getSleepTimer();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isDemo()Z", shift = At.Shift.BEFORE))
    public void kilt$postRenderSleepFade(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        post(poseStack, partialTick, VanillaGuiOverlay.SLEEP_FADE);
    }

    // TODO: Debug text and FPS graph render, that should go into DebugScreenOverlay

    // TODO: Potion icons, record overlay, subtitles, title, scoreboard, chat, player list
}
