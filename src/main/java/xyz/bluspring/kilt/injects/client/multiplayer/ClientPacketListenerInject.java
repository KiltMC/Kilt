// TRACKED HASH: 8fda2624182ac03df35817374f8cf966a4ed0fb0
package xyz.bluspring.kilt.injects.client.multiplayer;

import java.util.List;
import java.util.function.BooleanSupplier;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.CommandDispatcher;
import net.neoforged.neoforge.client.ClientCommandHandler;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.CreativeModeTabSearchRegistry;
import net.neoforged.neoforge.client.DimensionTransitionScreenManager;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.item.CreativeModeTabInjection;
import xyz.bluspring.kilt.injections.world.item.alchemy.PotionBrewingInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerInject extends ClientCommonPacketListenerImpl {
    @Shadow
    @Final
    private RegistryAccess.Frozen registryAccess;

    @Shadow
    protected abstract void startWaitingForNewLevel(LocalPlayer player, ClientLevel level, ReceivingLevelScreen.Reason reason);

    @Shadow
    private CommandDispatcher<SharedSuggestionProvider> commands;
    @Shadow
    @Final
    private RecipeManager recipeManager;
    private ConnectionType connectionType;

    protected ClientPacketListenerInject(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionBrewing;bootstrap(Lnet/minecraft/world/flag/FeatureFlagSet;)Lnet/minecraft/world/item/alchemy/PotionBrewing;"))
    private void kilt$storeConnectionType(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        this.connectionType = commonListenerCookie.connectionType();
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionBrewing;bootstrap(Lnet/minecraft/world/flag/FeatureFlagSet;)Lnet/minecraft/world/item/alchemy/PotionBrewing;"))
    private PotionBrewing kilt$trySetCurrentRegistryAccess(FeatureFlagSet enabledFeatures, Operation<PotionBrewing> original) {
        PotionBrewingInjection.kilt$registryAccess.set(this.registryAccess);
        var result = original.call(enabledFeatures);
        PotionBrewingInjection.kilt$registryAccess.set(RegistryAccess.EMPTY); // just in case
        return result;
    }

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetPos()V", shift = At.Shift.AFTER))
    public void kilt$fireForgeLoginEvent(ClientboundLoginPacket clientboundLoginPacket, CallbackInfo ci) {
        ClientHooks.firePlayerLogin(this.minecraft.gameMode, this.minecraft.player, this.minecraft.getConnection().getConnection());
    }

    @ModifyExpressionValue(method = "handleConfigurationStart", at = @At(value = "NEW", target = "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/network/Connection;Lnet/minecraft/client/multiplayer/CommonListenerCookie;)Lnet/minecraft/client/multiplayer/ClientConfigurationPacketListenerImpl;"))
    private ClientConfigurationPacketListenerImpl kilt$appendConnectionTypeToListener(ClientConfigurationPacketListenerImpl original) {
        // Kilt TODO: impl
//        original.kilt$setConnectionType(connectionType);
        return original;
    }

    @Unique private ResourceKey<Level> kilt$fromDimension;
    @Unique private ResourceKey<Level> kilt$toDimension;

    @WrapOperation(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;startWaitingForNewLevel(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/gui/screens/ReceivingLevelScreen$Reason;)V"))
    private void kilt$useNeoWaitingForLevel(ClientPacketListener instance, LocalPlayer player, ClientLevel level, ReceivingLevelScreen.Reason reason, Operation<Void> original, @Local(ordinal = 0) LocalPlayer originalPlayer, @Local(ordinal = 0) ResourceKey<Level> fromDimension, @Local(ordinal = 1) ResourceKey<Level> toDimension) {
        if (!originalPlayer.isDeadOrDying()) {
            this.kilt$fromDimension = fromDimension;
            this.kilt$toDimension = toDimension;
        }

        original.call(instance, player, level, reason);

        this.kilt$fromDimension = null;
        this.kilt$toDimension = null;
    }

    @Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetPos()V", shift = At.Shift.AFTER))
    public void kilt$fireForgeRespawnEvent(ClientboundRespawnPacket packet, CallbackInfo ci, @Local(ordinal = 0) LocalPlayer originalPlayer, @Local(ordinal = 1) LocalPlayer player) {
        ClientHooks.firePlayerRespawn(this.minecraft.gameMode, originalPlayer, player, player.connection.getConnection());
    }

    @Inject(method = "method_38542", at = @At("HEAD"), cancellable = true)
    public void kilt$onDataPacket(ClientboundBlockEntityDataPacket packet, BlockEntity blockEntity, CallbackInfo ci) {
        if (KiltHelper.INSTANCE.hasMethodOverride(blockEntity.getClass(), IBlockEntityExtension.class, "onDataPacket", Connection.class, ClientboundBlockEntityDataPacket.class, HolderLookup.Provider.class)) {
            blockEntity.onDataPacket(this.connection, packet, this.registryAccess);
            ci.cancel();
        }
    }

    @Unique
    private void startWaitingForNewLevel(LocalPlayer player, ClientLevel level, ReceivingLevelScreen.Reason reason, @Nullable ResourceKey<Level> toDimension, @Nullable ResourceKey<Level> fromDimension) {
        this.kilt$fromDimension = fromDimension;
        this.kilt$toDimension = toDimension;

        this.startWaitingForNewLevel(player, level, reason);

        this.kilt$fromDimension = null;
        this.kilt$toDimension = null;
    }

    @WrapOperation(method = "startWaitingForNewLevel", at = @At(value = "NEW", target = "Lnet/minecraft/client/gui/screens/ReceivingLevelScreen;"))
    private ReceivingLevelScreen kilt$tryUseNeoDimensionTransitionScreen(BooleanSupplier levelReceived, ReceivingLevelScreen.Reason reason, Operation<ReceivingLevelScreen> original) {
        if (this.kilt$fromDimension != null && this.kilt$toDimension != null && DimensionTransitionScreenManager.kilt$hasScreen(this.kilt$toDimension, this.kilt$fromDimension)) {
            return DimensionTransitionScreenManager.getScreen(this.kilt$toDimension, this.kilt$fromDimension).create(levelReceived, reason);
        }

        return original.call(levelReceived, reason);
    }

    @ModifyArg(method = "handleCommands", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket;getRoot(Lnet/minecraft/commands/CommandBuildContext;)Lcom/mojang/brigadier/tree/RootCommandNode;"))
    private CommandBuildContext kilt$storeCommandContext(CommandBuildContext ctx, @Share("context") LocalRef<CommandBuildContext> contextRef) {
        contextRef.set(ctx);
        return ctx;
    }

    @Inject(method = "handleCommands", at = @At("TAIL"))
    private void kilt$mergeCommands(ClientboundCommandsPacket packet, CallbackInfo ci, @Share("context") LocalRef<CommandBuildContext> context) {
        this.commands = ClientCommandHandler.mergeServerCommands(this.commands, context.get());
    }

    @Inject(method = "handleUpdateRecipes", at = @At("TAIL"))
    private void kilt$updateRecipes(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        ClientHooks.onRecipesUpdated(this.recipeManager);
    }

    @Inject(method = "handleUpdateTags", at = @At("TAIL"))
    private void kilt$updateCreativeTags(ClientboundUpdateTagsPacket packet, CallbackInfo ci) {
        var listener = (ClientPacketListener) (Object) this;

        CreativeModeTabs.allTabs().stream().filter(CreativeModeTabInjection::hasSearchBar)
            .forEach(tab -> {
                List<ItemStack> list = List.copyOf(tab.getDisplayItems());
                listener.searchTrees().updateCreativeTags(list, CreativeModeTabSearchRegistry.getTagSearchKey(tab));
            });
    }

    @ModifyVariable(method = "sendChat", at = @At("HEAD"), argsOnly = true)
    private String kilt$modifySendMessage(String message) {
        return ClientHooks.onClientSendMessage(message);
    }

    @Inject(method = "sendChat", at = @At(value = "INVOKE", target = "Ljava/time/Instant;now()Ljava/time/Instant;"), cancellable = true)
    private void kilt$cancelIfMessageEmpty(String message, CallbackInfo ci) {
        if (message.isEmpty())
            ci.cancel();
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void kilt$cancelIfCommandHandled(String command, CallbackInfo ci) {
        if (ClientCommandHandler.runCommand(command))
            ci.cancel();
    }

    @Inject(method = "sendUnsignedCommand", at = @At("HEAD"), cancellable = true)
    private void kilt$runUnsignedCommand(String command, CallbackInfoReturnable<Boolean> cir) {
        if (ClientCommandHandler.runCommand(command))
            cir.setReturnValue(true);
    }

    @Override
    public ConnectionType getConnectionType() {
        return connectionType;
    }
}
