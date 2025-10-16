// TRACKED HASH: 2bb3ae15b22cc5132705ea07857cbe14b92c3de6
package xyz.bluspring.kilt.injects.server.players;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.network.payload.ClientboundCustomSetTimePayload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.network.RegistryFriendlyByteBufInjection;

import java.util.function.Function;

@Mixin(PlayerList.class)
public abstract class PlayerListInject {
    @Shadow @Final private PlayerDataStorage playerIo;
    @Shadow @Final private MinecraftServer server;

    @ModifyExpressionValue(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryFriendlyByteBuf;decorator(Lnet/minecraft/core/RegistryAccess;)Ljava/util/function/Function;"))
    private Function<ByteBuf, RegistryFriendlyByteBuf> kilt$addConnectionTypeToBuf(Function<ByteBuf, RegistryFriendlyByteBuf> original, @Local ServerGamePacketListenerImpl packetListener) {
        return RegistryFriendlyByteBufInjection.kilt$wrappedDecorator(packetListener.getConnectionType(), original);
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundUpdateRecipesPacket;<init>(Ljava/util/Collection;)V"))
    private void kilt$syncDatapackRegistries(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new OnDatapackSyncEvent((PlayerList) (Object) this, player));
    }

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void kilt$firePlayerLoginEvent(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        EventHooks.firePlayerLoggedIn(player);
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;debug(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private void kilt$firePlayerLoadEvent(ServerPlayer player, CallbackInfoReturnable<CompoundTag> cir) {
        EventHooks.firePlayerLoadingEvent(player, this.playerIo, player.getUUID().toString());
    }

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void kilt$preventSaveIfNoConnection(ServerPlayer player, CallbackInfo ci) {
        if (player.connection == null)
            ci.cancel();
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void kilt$firePlayerLogoutEvent(ServerPlayer player, CallbackInfo ci) {
        EventHooks.firePlayerLoggedOut(player);
    }

    @Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/DimensionTransition;newLevel()Lnet/minecraft/server/level/ServerLevel;"))
    private void kilt$firePlayerRespawnPositionEvent(ServerPlayer player, boolean keepInventory, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir, @Local LocalRef<DimensionTransition> transition, @Share("event") LocalRef<PlayerRespawnPositionEvent> eventLocalRef) {
        var event = EventHooks.firePlayerRespawnPositionEvent(player, transition.get(), keepInventory);
        eventLocalRef.set(event);
        transition.set(event.getDimensionTransition());
    }

    @ModifyExpressionValue(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/DimensionTransition;missingRespawnBlock()Z"))
    private boolean kilt$checkShouldCopyOriginalSpawn(boolean original, @Share("event") LocalRef<PlayerRespawnPositionEvent> eventLocalRef) {
        if (eventLocalRef.get().kilt$hasCopyChanged())
            return eventLocalRef.get().copyOriginalSpawnPosition();

        return original;
    }

    @Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setHealth(F)V", shift = At.Shift.AFTER))
    private void kilt$firePlayerRespawnEvent(ServerPlayer player, boolean keepInventory, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir, @Local(ordinal = 1) ServerPlayer newPlayer) {
        EventHooks.firePlayerRespawnEvent(player, keepInventory);
    }

    @Inject(method = "op", at = @At("HEAD"), cancellable = true)
    private void kilt$checkPermissionChanged(GameProfile profile, CallbackInfo ci) {
        if (EventHooks.onPermissionChanged(profile, this.server.getOperatorUserPermissionLevel(), (PlayerList) (Object) this))
            ci.cancel();
    }

    @Inject(method = "deop", at = @At("HEAD"), cancellable = true)
    private void kilt$checkPermissionChangedDeop(GameProfile profile, CallbackInfo ci) {
        if (EventHooks.onPermissionChanged(profile, 0, (PlayerList) (Object) this))
            ci.cancel();
    }

    @ModifyArg(method = "sendLevelInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0))
    private Packet<?> kilt$tryUseCustomSetTime(Packet<?> original, @Local(argsOnly = true) ServerPlayer player, @Local(argsOnly = true) ServerLevel level) {
        if (player.connection.hasChannel(ClientboundCustomSetTimePayload.TYPE)) {
            return (new ClientboundCustomSetTimePayload(level.getGameTime(), level.getDayTime(), level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT), level.getDayTimeFraction(), level.getDayTimePerTick()))
                .toVanillaClientbound();
        }

        return original;
    }

    @WrapWithCondition(method = "getPlayerAdvancements", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;setPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private boolean kilt$avoidSetPlayerIfFake(PlayerAdvancements instance, ServerPlayer player) {
        return !(player instanceof FakePlayer);
    }

    @Inject(method = "reloadResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void kilt$syncDatapackOnReload(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new OnDatapackSyncEvent((PlayerList) (Object) this, null));
    }
}