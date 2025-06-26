// TRACKED HASH: 8a008dde196be8f110c6df462a387035cbfd879c
package xyz.bluspring.kilt.forgeinjects.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.Timer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.CreativeModeTabSearchRegistry;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.extensions.IForgeMinecraft;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.loading.ClientModLoader;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.ModLoader;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.client.ClientStartingCallback;
import xyz.bluspring.kilt.client.KiltClient;
import xyz.bluspring.kilt.injections.client.MinecraftInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
    @Shadow @Nullable public LocalPlayer player;
    @Unique
    private float realPartialTick;

    // This has to be public, it's a field that is used by the WorkaroundFixer.
    public Gui kilt$forgeGui = null;

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

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;Lnet/minecraft/client/renderer/RenderBuffers;)V", shift = At.Shift.AFTER))
    private void kilt$postRegisterStageEvent(GameConfig gameConfig, CallbackInfo ci) {
        ModLoader.get().postEvent(new net.minecraftforge.client.event.RenderLevelStageEvent.RegisterStageEvent());
    }

    @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;createSearchTrees()V"))
    private boolean kilt$delayModdedSearchTrees(Minecraft instance) {
        return false;
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;<init>(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/renderer/texture/TextureManager;)V", shift = At.Shift.BY, by = 2))
    private void kilt$postRegisterParticleProviders(GameConfig gameConfig, CallbackInfo ci) {
        ForgeHooksClient.onRegisterParticleProviders(this.particleEngine);
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

    @Unique private Map<CreativeModeTab, SearchRegistry.Key<ItemStack>> kilt$nameSearchKeys;
    @Unique private Map<CreativeModeTab, SearchRegistry.Key<ItemStack>> kilt$tagSearchKeys;
    @Unique private SearchRegistry.Key<ItemStack> kilt$nameSearchKey;
    @Unique private SearchRegistry.Key<ItemStack> kilt$tagSearchKey;

    @Inject(method = "createSearchTrees", at = @At("HEAD"))
    private void kilt$storeNameSearchKeys(CallbackInfo ci) {
        this.kilt$nameSearchKeys = CreativeModeTabSearchRegistry.getNameSearchKeys();
        this.kilt$tagSearchKeys = CreativeModeTabSearchRegistry.getTagSearchKeys();
    }

    @WrapOperation(method = "createSearchTrees", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/searchtree/SearchRegistry;register(Lnet/minecraft/client/searchtree/SearchRegistry$Key;Lnet/minecraft/client/searchtree/SearchRegistry$TreeBuilderSupplier;)V", ordinal = 0))
    private <T> void kilt$searchMultipleNameKeys(SearchRegistry instance, SearchRegistry.Key<T> key, SearchRegistry.TreeBuilderSupplier<T> factory, Operation<Void> original) {
        for (SearchRegistry.Key<ItemStack> nameSearchKey : this.kilt$nameSearchKeys.values()) {
            original.call(instance, nameSearchKey, factory);
        }
    }

    @WrapOperation(method = "createSearchTrees", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/searchtree/SearchRegistry;register(Lnet/minecraft/client/searchtree/SearchRegistry$Key;Lnet/minecraft/client/searchtree/SearchRegistry$TreeBuilderSupplier;)V", ordinal = 1))
    private <T> void kilt$searchMultipleTagKeys(SearchRegistry instance, SearchRegistry.Key<T> key, SearchRegistry.TreeBuilderSupplier<T> factory, Operation<Void> original) {
        for (SearchRegistry.Key<ItemStack> tagSearchKey : this.kilt$tagSearchKeys.values()) {
            original.call(instance, tagSearchKey, factory);
        }
    }

    @WrapOperation(method = "createSearchTrees", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;setSearchTreeBuilder(Ljava/util/function/Consumer;)V"))
    private void kilt$setMultipleSearchTreeBuilders(CreativeModeTab instance, Consumer<List<ItemStack>> searchTreeBuilder, Operation<Void> original) {
        this.kilt$nameSearchKeys.forEach((tab, nameSearchKey) -> {
            this.kilt$nameSearchKey = nameSearchKey;
            this.kilt$tagSearchKey = this.kilt$tagSearchKeys.get(tab);

            original.call(tab, searchTreeBuilder);

            this.kilt$nameSearchKey = null;
            this.kilt$tagSearchKey = null;
        });
    }

    @WrapOperation(method = "method_46740", at = @At(value = "FIELD", target = "Lnet/minecraft/client/searchtree/SearchRegistry;CREATIVE_NAMES:Lnet/minecraft/client/searchtree/SearchRegistry$Key;"))
    private SearchRegistry.Key<ItemStack> kilt$useNameSearchKey(Operation<SearchRegistry.Key<ItemStack>> original) {
        if (this.kilt$nameSearchKey == null)
            return original.call();

        return this.kilt$nameSearchKey;
    }

    @WrapOperation(method = "method_46740", at = @At(value = "FIELD", target = "Lnet/minecraft/client/searchtree/SearchRegistry;CREATIVE_TAGS:Lnet/minecraft/client/searchtree/SearchRegistry$Key;"))
    private SearchRegistry.Key<ItemStack> kilt$useTagSearchKey(Operation<SearchRegistry.Key<ItemStack>> original) {
        if (this.kilt$tagSearchKey == null)
            return original.call();

        return this.kilt$tagSearchKey;
    }

    // We're not using the Forge GUI system properly, but we're gonna make this incredibly mod compatible if we can.
    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/entity/ItemRenderer;)Lnet/minecraft/client/gui/Gui;"))
    private Gui kilt$initForgeGui(Minecraft minecraft, ItemRenderer itemRenderer, Operation<Gui> original) {
        var gui = original.call(minecraft, itemRenderer);

        this.kilt$forgeGui = new ForgeGui(minecraft);
        KiltClient.Companion.setForgeGui(this.kilt$getForgeGui());

        return gui;
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
        var inputEvent = ForgeHooksClient.onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND);
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

    @Override
    public ForgeGui kilt$getForgeGui() {
        return (ForgeGui) this.kilt$forgeGui;
    }
}