// TRACKED HASH: 2bb3ae15b22cc5132705ea07857cbe14b92c3de6
package xyz.bluspring.kilt.injects.server.players;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.NetworkHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class PlayerListInject {
    @Shadow @Final private PlayerDataStorage playerIo;

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getGameRules()Lnet/minecraft/world/level/GameRules;"))
    private void kilt$sendRegistryPacketsToPlayer(Connection netManager, ServerPlayer player, CallbackInfo ci) {
        NetworkHooks.sendMCRegistryPackets(netManager, "PLAY_TO_CLIENT");
    }

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 5))
    private void kilt$syncDatapackRegistries(Connection netManager, ServerPlayer player, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new OnDatapackSyncEvent((PlayerList) (Object) this, player));
    }

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void kilt$firePlayerLoginEvent(Connection netManager, ServerPlayer player, CallbackInfo ci) {
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

    @Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setHealth(F)V", shift = At.Shift.AFTER))
    private void kilt$firePlayerRespawnEvent(ServerPlayer player, boolean keepEverything, CallbackInfoReturnable<ServerPlayer> cir, @Local(ordinal = 1) ServerPlayer newPlayer) {
        EventHooks.firePlayerRespawnEvent(newPlayer, keepEverything);
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

    @WrapWithCondition(method = "getPlayerAdvancements", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;setPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private boolean kilt$avoidSetPlayerIfFake(PlayerAdvancements instance, ServerPlayer player) {
        return !(player instanceof FakePlayer);
    }

    @Inject(method = "reloadResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void kilt$syncDatapackOnReload(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new OnDatapackSyncEvent((PlayerList) (Object) this, null));
    }
}