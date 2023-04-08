package xyz.bluspring.kilt.forgeinjects.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.network.MemoryServerHandshakePacketListenerImpl;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MemoryServerHandshakePacketListenerImpl.class)
public class MemoryServerHandshakePacketListenerImplInject {
    @Shadow @Final private Connection connection;
}
