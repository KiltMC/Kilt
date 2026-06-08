// TRACKED HASH: 8a008dde196be8f110c6df462a387035cbfd879c
package xyz.bluspring.kilt.injects.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.DimensionTransitionScreenManager;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.extensions.IMinecraftExtension;
import net.neoforged.neoforge.client.loading.ClientModLoader;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.client.ClientStartingCallback;
import xyz.bluspring.kilt.injections.client.MinecraftInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@Mixin(Minecraft.class)
public abstract class MinecraftInject implements MinecraftInjection, IMinecraftExtension {
    @Shadow @Final @Mutable private ItemColors itemColors;
    @Shadow @Final private ReloadableResourceManager resourceManager;
    @Shadow @Final public Options options;
    @Shadow @Final public ParticleEngine particleEngine;
    @Shadow @Final private PackRepository resourcePackRepository;
    @Shadow public abstract BlockColors getBlockColors();
    @Mutable @Shadow @Final private BlockColors blockColors;
    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Nullable public HitResult hitResult;
    @Shadow @Final private DeltaTracker.Timer timer;
    @Shadow @Nullable public ClientLevel level;
    @Shadow @Nullable public MultiPlayerGameMode gameMode;

    @Inject(method = "getBlockColors", at = @At("HEAD"))
    private void kilt$workaroundEmptyBlockColors(CallbackInfoReturnable<BlockColors> cir) {
        if (this.blockColors == null)
            this.blockColors = BlockColors.createDefault();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$callStartingEvent(GameConfig gameConfig, CallbackInfo ci) {
        ClientStartingCallback.EVENT.invoker().onClientStarting((Minecraft) (Object) this);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;reload()V"), method = "<init>")
    public void kilt$initializeClientModLoader(GameConfig gameConfig, CallbackInfo ci) {
        ClientModLoader.begin((Minecraft) (Object) this, this.resourcePackRepository, this.resourceManager);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;Lnet/minecraft/client/renderer/RenderBuffers;)V", shift = At.Shift.AFTER))
    private void kilt$postRegisterStageEvent(GameConfig gameConfig, CallbackInfo ci) {
        ModLoader.postEvent(new RenderLevelStageEvent.RegisterStageEvent());
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;updateVsync(Z)V", shift = At.Shift.BEFORE), method = "<init>")
    public void kilt$initializeForge(GameConfig gameConfig, CallbackInfo ci) {
        ClientHooks.initClientHooks((Minecraft) (Object) this, this.resourceManager);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;<init>(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/renderer/texture/TextureManager;)V", shift = At.Shift.BY, by = 2))
    private void kilt$postRegisterParticleProviders(GameConfig gameConfig, CallbackInfo ci) {
        ClientHooks.onRegisterParticleProviders(this.particleEngine);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 1, shift = At.Shift.BEFORE), method = "runTick")
    public void kilt$setPartialTicks(boolean bl, CallbackInfo ci) {
        ClientHooks.fireRenderFramePre(this.timer);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V", shift = At.Shift.BY, by = 2), method = "runTick")
    public void kilt$callRenderTickEnd(boolean bl, CallbackInfo ci) {
        ClientHooks.fireRenderFramePost(this.timer);
    }

    @ModifyReturnValue(method = "buildInitialScreens", at = @At("RETURN"))
    private Runnable kilt$finishModLoading(Runnable original) {;
        return ClientModLoader.completeModLoading(original);
    }

    @WrapOperation(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState kilt$checkIsEmptyBlock(ClientLevel instance, BlockPos blockPos, Operation<BlockState> original, @Local BlockPos pos, @Share("has_override") LocalBooleanRef hasOverride, @Share("is_empty") LocalBooleanRef isEmpty) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), LevelReader.class, "isEmptyBlock", BlockPos.class)) {
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

    @Inject(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/BlockHitResult;getDirection()Lnet/minecraft/core/Direction;"), cancellable = true)
    private void kilt$onClickInputEvent(boolean leftClick, CallbackInfo ci, @Local BlockHitResult blockHitResult, @Local BlockPos blockPos, @Share("event") LocalRef<InputEvent.InteractionKeyMappingTriggered> eventRef) {
        var inputEvent = ClientHooks.onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND);
        eventRef.set(inputEvent);
        if (inputEvent.isCanceled()) {
            if (inputEvent.shouldSwingHand()) {
                this.particleEngine.addBlockHitEffects(blockPos, blockHitResult);
                this.player.swing(InteractionHand.MAIN_HAND);
            }
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private boolean kilt$checkHandSwing(boolean original, @Share("event") LocalRef<InputEvent.InteractionKeyMappingTriggered> eventRef) {
        return original && eventRef.get().shouldSwingHand();
    }

    @WrapOperation(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;crack(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V"))
    private void kilt$useKiltBlockHitEffects(ParticleEngine instance, BlockPos blockPos, Direction direction, Operation<Void> original, @Local BlockHitResult blockHitResult) {
        instance.kilt$addBlockHitEffects(blockPos, blockHitResult, direction, original);
    }

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"), cancellable = true)
    private void kilt$onAttackClickInputEvent(CallbackInfoReturnable<Boolean> cir, @Share("inputEvent") LocalRef<InputEvent.InteractionKeyMappingTriggered> inputEvent, @Local boolean flag) {
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
    private void kilt$callForgeUseInputEvent(CallbackInfo ci, @Share("inputEvent") LocalRef<InputEvent.InteractionKeyMappingTriggered> inputEvent, @Local InteractionHand hand) {
        inputEvent.set(ClientHooks.onClickInput(1, this.options.keyUse, hand));

        if (inputEvent.get().isCanceled()) {
            if (inputEvent.get().shouldSwingHand())
                this.player.swing(hand);

            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionResult;shouldSwing()Z"))
    private boolean kilt$onlySwingHandIfNeeded(boolean original, @Share("inputEvent") LocalRef<InputEvent.InteractionKeyMappingTriggered> inputEvent) {
        return original && (inputEvent.get() == null || inputEvent.get().shouldSwingHand());
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1))
    private void rightClickAir(CallbackInfo ci, @Local ItemStack stack, @Local InteractionHand hand) {
        if (stack.isEmpty() && (this.hitResult == null || this.hitResult.getType() == HitResult.Type.MISS)) {
            CommonHooks.onEmptyClick(this.player, hand);
        }
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void kilt$tryHandleUnloadLevel(ClientLevel level, ReceivingLevelScreen.Reason reason, CallbackInfo ci) {
        if (this.level != null) {
            NeoForge.EVENT_BUS.post(new LevelEvent.Unload(this.level));
        }
    }

    @WrapOperation(method = "setLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;updateScreenAndTick(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void kilt$tryUseNeoTransition(Minecraft instance, Screen screen, Operation<Void> original, @Local(argsOnly = true) ClientLevel level, @Local(argsOnly = true) ReceivingLevelScreen.Reason reason) {
        ResourceKey<Level> fromDim = null;
        ResourceKey<Level> toDim = null;
        if (level != null)
            toDim = level.dimension();

        if (this.level != null)
            fromDim = this.level.dimension();

        if (DimensionTransitionScreenManager.kilt$hasScreen(toDim, fromDim)) {
            original.call(instance, DimensionTransitionScreenManager.getScreenFromLevel(level, this.level).create(() -> false, reason));
        } else {
            original.call(instance, screen);
        }
    }

    @Definition(id = "gameMode", field = "Lnet/minecraft/client/Minecraft;gameMode:Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;")
    @Expression("this.gameMode = null")
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$firePlayerLogoutEvent(Screen nextScreen, boolean keepResourcePacks, CallbackInfo ci) {
        ClientHooks.firePlayerLogout(this.gameMode, this.player);
    }

    // Kilt: we're not reverting registries thanks

    @Definition(id = "integratedServer", local = @Local(type = IntegratedServer.class))
    @Expression("integratedServer != null")
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$handleUnloadEvent(Screen nextScreen, boolean keepResourcePacks, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new LevelEvent.Unload(this.level));
    }

    @Inject(method = "pickBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;instabuild:Z", ordinal = 0, opcode = Opcodes.GETFIELD), cancellable = true)
    private void kilt$callForgePickInputEvent(CallbackInfo ci) {
        if (ClientHooks.onClickInput(2, this.options.keyPickItem, InteractionHand.MAIN_HAND).isCanceled())
            ci.cancel();
    }

    @WrapOperation(method = "pickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getCloneItemStack(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack kilt$tryUseForgeCloneItemStack(Block instance, LevelReader levelReader, BlockPos blockPos, BlockState blockState, Operation<ItemStack> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IBlockExtension.class, "getCloneItemStack", BlockState.class, HitResult.class, LevelReader.class, BlockPos.class, Player.class)) {
            return instance.getCloneItemStack(blockState, this.hitResult, levelReader, blockPos, this.player);
        }

        return original.call(instance, levelReader, blockPos, blockState);
    }

    @WrapOperation(method = "pickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getPickResult()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack kilt$tryUseForgePickedResult(Entity instance, Operation<ItemStack> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IEntityExtension.class, "getPickedResult", HitResult.class)) {
            return instance.getPickedResult(this.hitResult);
        }

        var result = original.call(instance);

        if (result == null) {
            SpawnEggItem egg = DeferredSpawnEggItem.deferredOnlyById(instance.getType());
            if (egg != null)
                result = new ItemStack(egg);
            else
                result = ItemStack.EMPTY;
        }

        return result;
    }

    @Override
    public ItemColors getItemColors() {
        if (this.itemColors == null)
            this.itemColors = ItemColors.createDefault(this.getBlockColors());

        return this.itemColors;
    }
}
