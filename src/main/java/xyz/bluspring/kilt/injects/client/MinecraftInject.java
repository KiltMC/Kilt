package xyz.bluspring.kilt.injects.client;

import java.util.function.BooleanSupplier;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.platform.Window;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.ClientLifecycleHooks;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.extensions.IMinecraftExtension;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.loading.ClientModLoader;
import net.neoforged.neoforge.client.model.UnbakedModelParser;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.blaze3d.pipeline.MainTargetInjection;
import xyz.bluspring.kilt.injections.client.MinecraftInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.ClientBootstrap;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@Mixin(Minecraft.class)
public abstract class MinecraftInject implements MinecraftInjection, IMinecraftExtension {
    @Shadow @Final private PackRepository resourcePackRepository;
    @Shadow @Final private ParticleResources particleResources;
    @Shadow @Final private ReloadableResourceManager resourceManager;
    @Shadow private boolean gameLoadFinished;
    @Shadow @Nullable public Screen screen;
    @Shadow private volatile boolean pause;
    @Shadow @Final private Window window;
    @Shadow public abstract boolean isRunning();
    @Shadow @Final public Options options;
    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Nullable public ClientLevel level;
    @Shadow @Nullable public HitResult hitResult;
    @Shadow @Nullable public MultiPlayerGameMode gameMode;

    @Definition(id = "DEBUG_SYNCHRONOUS_GL_LOGS", field = "Lnet/minecraft/SharedConstants;DEBUG_SYNCHRONOUS_GL_LOGS:Z")
    @Definition(id = "renderDebugLabels", field = "Lnet/minecraft/client/main/GameConfig$GameData;renderDebugLabels:Z")
    @Expression(value = "DEBUG_SYNCHRONOUS_GL_LOGS", id = "glLogs")
    @Expression(value = "?.renderDebugLabels", id = "renderDebugLabels")
    @ModifyExpressionValue(method = "<init>", at = {
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "glLogs"),
        @At(value = "MIXINEXTRAS:EXPRESSION", id = "renderDebugLabels"),
    })
    private boolean kilt$checkEnableGlDebug(boolean original) {
        return original || FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.DEBUG_OPENGL);
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(II)Lcom/mojang/blaze3d/pipeline/MainTarget;"))
    private MainTarget kilt$tryInstantiateMainTarget(int desiredWidth, int desiredHeight, Operation<MainTarget> original) {
        return ClientHooks.kilt$instantiateMainTarget(isStencilEnabled ->
            isStencilEnabled ? MainTargetInjection.create(desiredWidth, desiredHeight, true) : original.call(desiredWidth, desiredHeight));
    }

    @Definition(id = "resourcePackRepository", field = "Lnet/minecraft/client/Minecraft;resourcePackRepository:Lnet/minecraft/server/packs/repository/PackRepository;")
    @Definition(id = "reload", method = "Lnet/minecraft/server/packs/repository/PackRepository;reload()V")
    @Expression("this.resourcePackRepository.reload()")
    @Inject(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$bootstrapRepositories(CallbackInfo ci) {
        ClientModLoader.setupModResourcePacks(this.resourcePackRepository);
        ClientBootstrap.bootstrap();
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/block/BlockColors;createDefault()Lnet/minecraft/client/color/block/BlockColors;", shift = At.Shift.AFTER))
    private void kilt$initUnbakedModelParser(CallbackInfo ci) {
        UnbakedModelParser.init();
    }

    @Definition(id = "onReload", method = "Lnet/minecraft/client/particle/ParticleResources;onReload(Ljava/lang/Runnable;)V")
    @Definition(id = "particleEngine", field = "Lnet/minecraft/client/Minecraft;particleEngine:Lnet/minecraft/client/particle/ParticleEngine;")
    @Definition(id = "clearParticles", method = "Lnet/minecraft/client/particle/ParticleEngine;clearParticles()V")
    @Expression("?.onReload(this.particleEngine::clearParticles)")
    @Inject(method = "<init>", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void kilt$initParticleProviders(GameConfig gameConfig, CallbackInfo ci) {
        ClientHooks.onRegisterParticleProviders(this.particleResources);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;updateVsync(Z)V"))
    private void kilt$initClientHooks(GameConfig gameConfig, CallbackInfo ci) {
        ClientHooks.initClientHooks((Minecraft) (Object) this, this.resourceManager);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;registerPanoramaTextures(Lnet/minecraft/client/renderer/texture/TextureManager;)V", shift = At.Shift.AFTER))
    private void kilt$finisClientModLoading(GameConfig gameConfig, CallbackInfo ci) {
        ClientModLoader.finish();
    }

    @Inject(method = "onResourceLoadFinished", at = @At("HEAD"))
    private void kilt$fireResourceLoadFinish(CallbackInfo ci) {
        ClientHooks.fireResourceLoadFinishedEvent(!this.gameLoadFinished);
    }

    @ModifyReturnValue(method = "buildInitialScreens", at = @At("RETURN"))
    private Runnable kilt$completeModLoading(Runnable original) {
        return ClientModLoader.completeModLoading(original);
    }

    @Definition(id = "screen", field = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;")
    @Definition(id = "screenVar", local = @Local(type = Screen.class, name = "screen", argsOnly = true))
    @Expression("this.screen = screenVar")
    @ModifyVariable(method = "setScreen", at = @At("MIXINEXTRAS:EXPRESSION"), argsOnly = true, name = "screen")
    private Screen kilt$handleGuiChangeEvent(Screen screen, @Cancellable CallbackInfo ci) {
        ClientHooks.clearGuiLayers((Minecraft) (Object) this);
        Screen old = this.screen;

        if (screen != null) {
            var event = new ScreenEvent.Opening(old, screen);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                ci.cancel();
                return screen;
            }

            screen = event.getNewScreen();
        }

        if (old != null && screen != old) {
            NeoForge.EVENT_BUS.post(new ScreenEvent.Closing(old));
            old.removed();
        }

        return screen;
    }

    @Definition(id = "LOGGER", field = "Lnet/minecraft/client/Minecraft;LOGGER:Lorg/slf4j/Logger;")
    @Definition(id = "info", method = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V")
    @Expression("LOGGER.info('Stopping!')")
    @Inject(method = "destroy", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$handleClientStoppingEvent(CallbackInfo ci) {
        ClientLifecycleHooks.handleClientStopping((Minecraft) (Object) this);
    }

    @Inject(method = "destroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;close()V", shift = At.Shift.AFTER))
    private void kilt$handleClientStoppedEvent(CallbackInfo ci) {
        ClientLifecycleHooks.handleClientStopped((Minecraft) (Object) this);
    }

    @Definition(id = "pause", field = "Lnet/minecraft/client/Minecraft;pause:Z")
    @Expression("this.pause = ?")
    @WrapOperation(method = "runTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$checkShouldPauseEvent(Minecraft instance, boolean value, Operation<Void> original) {
        if (value != this.pause && !ClientHooks.onClientPauseChangePre(value)) {
            original.call(instance, value);
            ClientHooks.onClientPauseChangePost(value);
        }
    }

    @WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private void kilt$wrapRenderFrameEvent(GameRenderer instance, DeltaTracker deltaTracker, boolean advanceGameTime, Operation<Void> original) {
        ClientHooks.fireRenderFramePre(deltaTracker);
        original.call(instance, deltaTracker, advanceGameTime);
        ClientHooks.fireRenderFramePost(deltaTracker);
    }

    @Definition(id = "screen", field = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;")
    @Expression("this.screen != null")
    @Inject(method = "resizeGui", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$handleWindowResizeEvent(CallbackInfo ci) {
        ClientHooks.fireWindowResize(this.window);
    }

    @Inject(method = "resizeGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;resize(II)V", shift = At.Shift.AFTER))
    private void kilt$resizeGuiLayers(CallbackInfo ci) {
        ClientHooks.resizeGuiLayers(this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void kilt$handleShuttingDownEvent(CallbackInfo ci) {
        if (this.isRunning())
            NeoForge.EVENT_BUS.post(new GameShuttingDownEvent());
    }

    @WrapOperation(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState kilt$checkIsEmptyBlock(ClientLevel instance, BlockPos blockPos, Operation<BlockState> original, @Share("has_override") LocalBooleanRef hasOverride, @Share("is_empty") LocalBooleanRef isEmpty) {
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(instance.getClass(), LevelReader.class, "isEmptyBlock", boolean.class, BlockPos.class)) {
            hasOverride.set(true);
            isEmpty.set(instance.isEmptyBlock(blockPos));
            return Blocks.AIR.defaultBlockState();
        }

        return original.call(instance, blockPos);
    }

    @WrapOperation(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean kilt$replaceIsAirCheck(BlockState instance, Operation<Boolean> original, @Share("has_override") LocalBooleanRef hasOverride, @Share("is_empty") LocalBooleanRef isEmpty) {
        if (hasOverride.get()) {
            return isEmpty.get();
        }
        return original.call(instance);
    }

    @Inject(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"), cancellable = true)
    private void kilt$onClickInputEvent(boolean down, CallbackInfo ci, @Local(name = "direction") Direction direction, @Local(name = "blockHit") BlockHitResult blockHitResult, @Local(name = "pos") BlockPos blockPos, @Share("event") LocalRef<InputEvent.InteractionKeyMappingTriggered> eventRef) {
        var inputEvent = ClientHooks.onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND);
        eventRef.set(inputEvent);
        if (inputEvent.isCanceled()) {
            if (inputEvent.shouldSwingHand()) {
                this.level.addBreakingBlockEffect(blockPos, direction, blockHitResult);
                this.player.swing(InteractionHand.MAIN_HAND);
            }
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private boolean kilt$checkHandSwing(boolean original, @Share("event") LocalRef<InputEvent.InteractionKeyMappingTriggered> eventRef) {
        return original && eventRef.get().shouldSwingHand();
    }

    @WrapOperation(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addBreakingBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V"))
    private void kilt$useKiltBlockHitEffects(ClientLevel instance, BlockPos pos, Direction direction, Operation<Void> original, @Local(name = "blockHit") BlockHitResult blockHitResult) {
        var ext = IClientBlockExtensions.of(instance.getBlockState(pos));
        if (ext != IClientBlockExtensions.DEFAULT) {
            instance.addBreakingBlockEffect(pos, direction, blockHitResult);
        } else {
            original.call(instance, pos, direction);
        }
    }

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"), cancellable = true)
    private void kilt$onAttackClickInputEvent(CallbackInfoReturnable<Boolean> cir, @Share("inputEvent") LocalRef<InputEvent.InteractionKeyMappingTriggered> inputEvent, @Local(name = "endAttack") boolean flag) {
        inputEvent.set(ClientHooks.onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND));

        if (inputEvent.get().isCanceled()) {
            if (inputEvent.get().shouldSwingHand())
                this.player.swing(InteractionHand.MAIN_HAND);

            cir.setReturnValue(flag);
        }
    }

    @WrapWithCondition(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private boolean kilt$swingHandIfEventPermits(LocalPlayer instance, InteractionHand interactionHand, @Share("inputEvent") LocalRef<InputEvent.InteractionKeyMappingTriggered> inputEvent) {
        return inputEvent.get() == null || inputEvent.get().shouldSwingHand();
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", ordinal = 0), cancellable = true)
    private void kilt$callForgeUseInputEvent(CallbackInfo ci, @Share("inputEvent") LocalRef<InputEvent.InteractionKeyMappingTriggered> inputEvent, @Local(name = "hand") InteractionHand hand) {
        inputEvent.set(ClientHooks.onClickInput(1, this.options.keyUse, hand));

        if (inputEvent.get().isCanceled()) {
            if (inputEvent.get().shouldSwingHand())
                this.player.swing(hand);

            ci.cancel();
        }
    }

    @WrapWithCondition(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private boolean kilt$onlySwingHandIfNeeded(LocalPlayer instance, InteractionHand hand, @Share("inputEvent") LocalRef<InputEvent.InteractionKeyMappingTriggered> inputEvent) {
        return inputEvent.get() == null || inputEvent.get().shouldSwingHand();
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1))
    private void rightClickAir(CallbackInfo ci, @Local(name = "heldItem") ItemStack stack, @Local(name = "hand") InteractionHand hand) {
        if (stack.isEmpty() && (this.hitResult == null || this.hitResult.getType() == HitResult.Type.MISS)) {
            CommonHooks.onEmptyClick(this.player, hand);
        }
    }

    @Definition(id = "level", field = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;")
    @Expression("this.level != null")
    @Inject(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$fireClientPreTickEvent(CallbackInfo ci) {
        if (this.gameLoadFinished) {
            ClientHooks.fireClientTickPre();
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;tick(Ljava/util/function/BooleanSupplier;)V"))
    private void kilt$fireLevelTickEvent(ClientLevel instance, BooleanSupplier haveTime, Operation<Void> original) {
        EventHooks.fireLevelTickPre(instance, haveTime);
        original.call(instance, haveTime);
        EventHooks.fireLevelTickPost(instance, haveTime);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void kilt$fireClientPostTickEvent(CallbackInfo ci) {
        if (this.gameLoadFinished) {
            ClientHooks.fireClientTickPost();
        }
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void kilt$tryHandleUnloadLevel(ClientLevel level, CallbackInfo ci) {
        if (this.level != null) {
            NeoForge.EVENT_BUS.post(new LevelEvent.Unload(this.level));
        }
    }

    // Kilt: we're not reverting registries thanks

    @Definition(id = "gameMode", field = "Lnet/minecraft/client/Minecraft;gameMode:Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;")
    @Expression("this.gameMode = null")
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$firePlayerLogoutEvent(Screen screen, boolean keepResourcePacks, boolean stopSound, CallbackInfo ci) {
        ClientHooks.firePlayerLogout(this.gameMode, this.player);
    }

    @Definition(id = "integratedServer", local = @Local(type = IntegratedServer.class))
    @Expression("integratedServer != null")
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$handleUnloadEvent(Screen screen, boolean keepResourcePacks, boolean stopSound, CallbackInfo ci) {
        if (this.level != null)
            NeoForge.EVENT_BUS.post(new LevelEvent.Unload(this.level));
    }

    @Definition(id = "level", field = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;")
    @Expression("this.level = null")
    @Inject(method = "clearClientLevel", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$callUnloadEventInClear(Screen screen, CallbackInfo ci) {
        if (this.level != null) {
            NeoForge.EVENT_BUS.post(new LevelEvent.Unload(this.level));
        }
    }

    @Inject(method = "pickBlockOrEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;hasControlDown()Z", ordinal = 0), cancellable = true)
    private void kilt$checkHandleClickInput(CallbackInfo ci) {
        if (ClientHooks.onClickInput(2, this.options.keyPickItem, InteractionHand.MAIN_HAND).isCanceled())
            ci.cancel();
    }

    // Kilt: do we need to handle tryGetDevice stuff?

    @Override
    @Accessor
    public abstract BlockModelResolver getBlockModelResolver();
}
