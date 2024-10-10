// TRACKED HASH: 8fda2624182ac03df35817374f8cf966a4ed0fb0
package xyz.bluspring.kilt.forgeinjects.client.multiplayer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.network.NetworkHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.bluspring.kilt.injections.client.player.LocalPlayerInjection;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerInject {
    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final private Connection connection;

    @Shadow @Final private RecipeManager recipeManager;

    @Shadow public CommandDispatcher<SharedSuggestionProvider> commands;

    @Shadow public abstract RegistryAccess registryAccess();

    @Shadow private LayeredRegistryAccess<ClientRegistryLayer> registryAccess;

    @Inject(method = "handleLogin", at = @At(value= "INVOKE", target = "Lnet/minecraft/client/Options;setServerRenderDistance(I)V", shift = At.Shift.AFTER))
    public void kilt$sendMcRegistryPackets(ClientboundLoginPacket packet, CallbackInfo ci) {
        NetworkHooks.sendMCRegistryPackets(this.connection, "PLAY_TO_SERVER");
    }

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetPos()V", shift = At.Shift.AFTER))
    public void kilt$fireForgeLoginEvent(ClientboundLoginPacket clientboundLoginPacket, CallbackInfo ci) {
        ForgeHooksClient.firePlayerLogin(this.minecraft.gameMode, this.minecraft.player, this.minecraft.getConnection().getConnection());
    }

    @Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetPos()V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void kilt$updateSyncFields(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo ci, ResourceKey resourceKey, Holder holder, LocalPlayer localPlayer, int i, String string, LocalPlayer localPlayer2) {
        ((LocalPlayerInjection) localPlayer2).updateSyncFields(localPlayer);
    }

    @Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setServerBrand(Ljava/lang/String;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void kilt$fireForgeRespawnEvent(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo ci, ResourceKey resourceKey, Holder holder, LocalPlayer localPlayer, int i, String string, LocalPlayer localPlayer2) {
        ForgeHooksClient.firePlayerRespawn(this.minecraft.gameMode, localPlayer, localPlayer2, localPlayer2.connection.getConnection());
    }

    @WrapOperation(method = "handleCommands", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/CommandBuildContext;simple(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/world/flag/FeatureFlagSet;)Lnet/minecraft/commands/CommandBuildContext;"))
    private CommandBuildContext kilt$storeCommandContext(HolderLookup.Provider provider, FeatureFlagSet enabledFeatures, Operation<CommandBuildContext> original, @Share("context") LocalRef<CommandBuildContext> context) {
        var ctx = original.call(provider, enabledFeatures);
        context.set(ctx);
        return ctx;
    }

    @WrapOperation(method = "method_38542", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundBlockEntityDataPacket;getTag()Lnet/minecraft/nbt/CompoundTag;"))
    public CompoundTag kilt$replaceWithDataPacketLoad(ClientboundBlockEntityDataPacket instance, Operation<CompoundTag> original, @Local(argsOnly = true) BlockEntity blockEntity) {
        var result = original.call(instance);

        if (result == null) {
            blockEntity.onDataPacket(this.connection, instance);
        }

        return result;
    }

    @Inject(method = "handleCommands", at = @At("TAIL"))
    private void kilt$mergeCommands(ClientboundCommandsPacket packet, CallbackInfo ci, @Share("context") LocalRef<CommandBuildContext> context) {
        this.commands = ClientCommandHandler.mergeServerCommands(this.commands, context.get());
    }

    @Inject(method = "handleUpdateRecipes", at = @At("TAIL"))
    private void kilt$updateRecipes(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        ForgeHooksClient.onRecipesUpdated(this.recipeManager);
    }

    @Inject(method = "handleUpdateTags", at = @At("TAIL"))
    private void kilt$updateTags(ClientboundUpdateTagsPacket packet, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TagsUpdatedEvent(this.registryAccess.compositeAccess(), true, this.connection.isMemoryConnection()));
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    public void kilt$runCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (NetworkHooks.onCustomPayload(packet, this.connection)) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "sendChat", at = @At("HEAD"))
    private String kilt$modifySendMessage(String message) {
        return ForgeHooksClient.onClientSendMessage(message);
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
}