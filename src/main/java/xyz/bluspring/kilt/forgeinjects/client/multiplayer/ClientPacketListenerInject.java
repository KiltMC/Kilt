package xyz.bluspring.kilt.forgeinjects.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
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

    @Inject(method = "handleLogin", at = @At(value= "INVOKE", target = "Lnet/minecraft/client/Options;setServerRenderDistance(I)V", shift = At.Shift.AFTER))
    public void kilt$sendMcRegistryPackets(ClientboundLoginPacket packet, CallbackInfo ci) {
        NetworkHooks.sendMCRegistryPackets(this.connection, "PLAY_TO_SERVER");
    }

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetPos()V", shift = At.Shift.AFTER))
    public void kilt$fireForgeLoginEvent(ClientboundLoginPacket clientboundLoginPacket, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingIn(this.minecraft.gameMode, this.minecraft.player, this.minecraft.getConnection().getConnection()));
    }

    @Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetPos()V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void kilt$updateSyncFields(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo ci, ResourceKey resourceKey, Holder holder, LocalPlayer localPlayer, int i, String string, LocalPlayer localPlayer2) {
        ((LocalPlayerInjection) localPlayer2).updateSyncFields(localPlayer);
    }

    @Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setServerBrand(Ljava/lang/String;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void kilt$fireForgeRespawnEvent(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo ci, ResourceKey resourceKey, Holder holder, LocalPlayer localPlayer, int i, String string, LocalPlayer localPlayer2) {
        MinecraftForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.Clone(this.minecraft.gameMode, localPlayer, localPlayer2, localPlayer2.connection.getConnection()));
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
