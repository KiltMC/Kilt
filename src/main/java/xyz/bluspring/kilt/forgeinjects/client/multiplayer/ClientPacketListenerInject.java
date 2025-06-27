package xyz.bluspring.kilt.forgeinjects.client.multiplayer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.network.NetworkHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.bluspring.kilt.injections.client.player.LocalPlayerInjection;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerInject {
    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final private Connection connection;

    @Shadow public CommandDispatcher<SharedSuggestionProvider> commands;

    @Shadow @Final private RecipeManager recipeManager;

    @Shadow private RegistryAccess.Frozen registryAccess;

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

    @WrapOperation(method = "method_38542", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundBlockEntityDataPacket;getTag()Lnet/minecraft/nbt/CompoundTag;"))
    public CompoundTag kilt$replaceWithDataPacketLoad(ClientboundBlockEntityDataPacket instance, Operation<CompoundTag> original, @Local(argsOnly = true) BlockEntity blockEntity) {
        var result = original.call(instance);

        if (result == null) {
            blockEntity.onDataPacket(this.connection, instance);
        }

        return result;
    }

    @WrapOperation(method = "handleCommands", at = @At(value = "NEW", target = "(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/commands/CommandBuildContext;"))
    private CommandBuildContext kilt$storeCommandContext(RegistryAccess registryAccess, Operation<CommandBuildContext> original, @Share("context") LocalRef<CommandBuildContext> context) {
        var ctx = original.call(registryAccess);
        context.set(ctx);
        return ctx;
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
        MinecraftForge.EVENT_BUS.post(new TagsUpdatedEvent(this.registryAccess, true, this.connection.isMemoryConnection()));
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    public void kilt$runCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (!minecraft.isSameThread() && NetworkHooks.onCustomPayload(packet, this.connection)) {
            ci.cancel();
        }
    }

    @Redirect(method = "method_38542", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundBlockEntityDataPacket;getTag()Lnet/minecraft/nbt/CompoundTag;"))
    public CompoundTag kilt$replaceWithDataPacketLoad(ClientboundBlockEntityDataPacket instance, ClientboundBlockEntityDataPacket unused, BlockEntity blockEntity) {
        blockEntity.onDataPacket(this.connection, instance);

        return null;
    }
}
