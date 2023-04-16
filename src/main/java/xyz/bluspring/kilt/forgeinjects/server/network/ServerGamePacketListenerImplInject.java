package xyz.bluspring.kilt.forgeinjects.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.network.NetworkHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplInject {
    @Shadow @Final public Connection connection;

    @Inject(at = @At("HEAD"), method = "handleCustomPayload")
    public void kilt$handleCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        NetworkHooks.onCustomPayload(packet, this.connection);
    }
}
