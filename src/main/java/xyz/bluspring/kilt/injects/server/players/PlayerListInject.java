package xyz.bluspring.kilt.injects.server.players;

import java.util.Optional;
import java.util.function.Function;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.netty.buffer.ByteBuf;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.network.RegistryFriendlyByteBufInjection;
import xyz.bluspring.kilt.injections.server.players.PlayerListInjection;

import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.PlayerDataStorage;

@Mixin(PlayerList.class)
public abstract class PlayerListInject implements PlayerListInjection {
    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    private PlayerDataStorage playerIo;

    @ModifyExpressionValue(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryFriendlyByteBuf;decorator(Lnet/minecraft/core/RegistryAccess;)Ljava/util/function/Function;"))
    private Function<ByteBuf, RegistryFriendlyByteBuf> kilt$attachConnectionTypeToDecorator(Function<ByteBuf, RegistryFriendlyByteBuf> original, @Local(name = "playerConnection") ServerGamePacketListenerImpl playerConnection) {
        return RegistryFriendlyByteBufInjection.kilt$wrappedDecorator(playerConnection.getConnectionType(), original);
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getRecipeManager()Lnet/minecraft/world/item/crafting/RecipeManager;", ordinal = 0))
    private void kilt$handleDatapackSyncEvent(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci, @Share("dataSyncEvent") LocalRef<OnDatapackSyncEvent> dataSyncEvent) {
        dataSyncEvent.set(NeoForge.EVENT_BUS.post(new OnDatapackSyncEvent((PlayerList) (Object) this, player)));
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void kilt$sendRecipesToPlayer(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci, @Share("dataSyncEvent") LocalRef<OnDatapackSyncEvent> dataSyncEvent, @Local(name = "recipeManager") RecipeManager recipeManager) {
        CommonHooks.sendRecipes(player, dataSyncEvent.get().getRecipeTypesToSend(), recipeManager.recipeMap());
    }

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void kilt$firePlayerJoinEvents(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        AttachmentSync.syncInitialPlayerAttachments(player);
        EventHooks.firePlayerLoggedIn(player);
    }

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void kilt$checkPlayerHasConnection(ServerPlayer player, CallbackInfo ci) {
        if (player.connection == null)
            ci.cancel();
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void kilt$firePlayerLeaveEvent(ServerPlayer player, CallbackInfo ci) {
        EventHooks.firePlayerLoggedOut(player);
    }

    @ModifyVariable(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removePlayerImmediately(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity$RemovalReason;)V", shift = At.Shift.AFTER), name = "respawnInfo")
    private TeleportTransition kilt$modifyRespawnInfo(TeleportTransition respawnInfo, @Share("event") LocalRef<PlayerRespawnPositionEvent> eventRef, @Local(argsOnly = true, name = "serverPlayer") ServerPlayer player, @Local(argsOnly = true, name = "keepAllPlayerData") boolean keepAllPlayerData) {
        var event = EventHooks.firePlayerRespawnPositionEvent(player, respawnInfo, keepAllPlayerData);
        eventRef.set(event);
        return event.getTeleportTransition();
    }

    @Definition(id = "respawnInfo", local = @Local(type = TeleportTransition.class, name = "respawnInfo"))
    @Definition(id = "missingRespawnBlock", method = "Lnet/minecraft/world/level/portal/TeleportTransition;missingRespawnBlock()Z")
    @Expression("respawnInfo.missingRespawnBlock() == false")
    @ModifyExpressionValue(method = "respawn", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkShouldCopyOriginalRespawnPos(boolean original, @Share("event") LocalRef<PlayerRespawnPositionEvent> eventRef) {
        if (eventRef.get() != null && eventRef.get().kilt$wasModified)
            return eventRef.get().copyOriginalSpawnPosition();

        return original;
    }

    @Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setHealth(F)V", shift = At.Shift.AFTER))
    private void kilt$firePlayerRespawnEvent(ServerPlayer serverPlayer, boolean keepAllPlayerData, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir, @Local(name = "player") ServerPlayer player) {
        AttachmentSync.syncInitialPlayerAttachments(player);
        EventHooks.firePlayerRespawnEvent(player, keepAllPlayerData);
    }

    @Inject(method = "op(Lnet/minecraft/server/players/NameAndId;Ljava/util/Optional;Ljava/util/Optional;)V", at = @At("HEAD"), cancellable = true)
    private void kilt$handlePermissionsChangedForOp(NameAndId nameAndId, Optional<LevelBasedPermissionSet> permissions, Optional<Boolean> canBypassPlayerLimit, CallbackInfo ci) {
        if (EventHooks.onPermissionChanged(nameAndId, this.server.operatorUserPermissions(), (PlayerList) (Object) this))
            ci.cancel();
    }

    @Inject(method = "deop", at = @At("HEAD"), cancellable = true)
    private void kilt$handlePermissionsChangedForDeop(NameAndId nameAndId, CallbackInfo ci) {
        if (EventHooks.onPermissionChanged(nameAndId, LevelBasedPermissionSet.ALL, (PlayerList) (Object) this))
            ci.cancel();
    }

    @Inject(method = "sendLevelInfo", at = @At("TAIL"))
    private void kilt$syncLevelAttachmentsToPlayer(ServerPlayer player, ServerLevel level, CallbackInfo ci) {
        AttachmentSync.syncInitialLevelAttachments(level, player);
    }

    @WrapWithCondition(method = "getPlayerAdvancements", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;setPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private boolean kilt$checkIsFakePlayer(PlayerAdvancements instance, ServerPlayer player) {
        return !(player instanceof FakePlayer);
    }

    @Definition(id = "broadcastAll", method = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V")
    @Definition(id = "ClientboundUpdateTagsPacket", type = ClientboundUpdateTagsPacket.class)
    @Expression("this.broadcastAll(new ClientboundUpdateTagsPacket(?))")
    @Inject(method = "reloadResources", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$syncDatapackOnReload(CallbackInfo ci, @Share("dataSyncEvent") LocalRef<OnDatapackSyncEvent> dataSyncEvent) {
        dataSyncEvent.set(NeoForge.EVENT_BUS.post(new OnDatapackSyncEvent((PlayerList) (Object) this, null)));
    }

    @Inject(method = "reloadResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/ServerRecipeBook;sendInitialRecipeBook(Lnet/minecraft/server/level/ServerPlayer;)V", shift = At.Shift.AFTER))
    private void kilt$syncRecipesToPlayer(CallbackInfo ci, @Share("dataSyncEvent") LocalRef<OnDatapackSyncEvent> dataSyncEvent, @Local(name = "player") ServerPlayer player, @Local(name = "recipeManager") RecipeManager recipeManager) {
        CommonHooks.sendRecipes(player, dataSyncEvent.get().getRecipeTypesToSend(), recipeManager.recipeMap());
    }

    @Accessor
    public abstract PlayerDataStorage getPlayerIo();
}
