// TRACKED HASH: 8a008dde196be8f110c6df462a387035cbfd879c
package xyz.bluspring.kilt.forgeinjects.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.Timer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.IForgeMinecraft;
import net.minecraftforge.client.loading.ClientModLoader;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.client.ClientStartingCallback;
import xyz.bluspring.kilt.injections.client.MinecraftInjection;

@Mixin(Minecraft.class)
public abstract class MinecraftInject implements MinecraftInjection, IForgeMinecraft {
    @Shadow @Final @Mutable
    private ItemColors itemColors;

    @Shadow @Final private SearchRegistry searchRegistry;
    @Shadow @Final private ReloadableResourceManager resourceManager;
    @Shadow @Final public Options options;
    @Shadow private volatile boolean pause;
    @Shadow private float pausePartialTick;
    @Shadow @Final private Timer timer;
    @Shadow @Final public ParticleEngine particleEngine;
    @Shadow @Final private PackRepository resourcePackRepository;

    @Shadow public abstract BlockColors getBlockColors();

    @Mutable
    @Shadow @Final private BlockColors blockColors;
    @Unique
    private float realPartialTick;

    @Override
    public float getPartialTick() {
        return realPartialTick;
    }

    @Inject(method = "getBlockColors", at = @At("HEAD"))
    private void kilt$workaroundEmptyBlockColors(CallbackInfoReturnable<BlockColors> cir) {
        if (this.blockColors == null)
            this.blockColors = BlockColors.createDefault();
    }

    @Override
    public ItemColors getItemColors() {
        if (this.itemColors == null)
            this.itemColors = ItemColors.createDefault(this.getBlockColors());

        return this.itemColors;
    }

    @Override
    public SearchRegistry getSearchTreeManager() {
        return this.searchRegistry;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;updateVsync(Z)V", shift = At.Shift.BEFORE), method = "<init>")
    public void kilt$initializeForge(GameConfig gameConfig, CallbackInfo ci) {
        ForgeHooksClient.initClientHooks((Minecraft) (Object) this, this.resourceManager);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$callStartingEvent(GameConfig gameConfig, CallbackInfo ci) {
        ClientStartingCallback.EVENT.invoker().onClientStarting((Minecraft) (Object) this);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;reload()V"), method = "<init>")
    public void kilt$initializeClientModLoader(GameConfig gameConfig, CallbackInfo ci) {
        ClientModLoader.begin((Minecraft) (Object) this, this.resourcePackRepository, this.resourceManager);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.BEFORE), method = "runTick")
    public void kilt$setPartialTicks(boolean bl, CallbackInfo ci) {
        realPartialTick = this.pause ? this.pausePartialTick : this.timer.partialTick;
        ForgeEventFactory.onRenderTickStart(realPartialTick);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V", shift = At.Shift.BY, by = 2), method = "runTick")
    public void kilt$callRenderTickEnd(boolean bl, CallbackInfo ci) {
        ForgeEventFactory.onRenderTickEnd(realPartialTick);
    }

    @Inject(method = "method_29338", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;onGameLoadFinished()V"))
    private void kilt$finishModLoading(CallbackInfo ci) {
        ClientModLoader.completeModLoading();
    }
}