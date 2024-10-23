// TRACKED HASH: edbba521b66a25813967d4ed08f1ca7f80422f08
package xyz.bluspring.kilt.forgeinjects.server.network;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerLoginPacketListenerImpl.class, priority = 1050)
public abstract class ServerLoginPacketListenerImplInject {
    @Shadow @Final public Connection connection;

    @Shadow @Nullable private ServerPlayer delayedAcceptPlayer;

    @Shadow private int tick;

    @Shadow public abstract void disconnect(Component component);

    @Shadow ServerLoginPacketListenerImpl.State state;

    @Inject(method = "handleCustomQueryPacket", at = @At("HEAD"), cancellable = true)
    public void kilt$handleCustomQuery(ServerboundCustomQueryPacket packet, CallbackInfo ci) {
        if (NetworkHooks.onCustomPayload(packet, this.connection)) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;state:Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;", ordinal = 0))
    private ServerLoginPacketListenerImpl.State kilt$tickNegotiatingState(ServerLoginPacketListenerImpl instance, Operation<ServerLoginPacketListenerImpl.State> original) {
        var currentState = original.call(instance);

        if (currentState == ServerLoginPacketListenerImpl.State.NEGOTIATING) {
            if (NetworkHooks.tickNegotiation((ServerLoginPacketListenerImpl) (Object) this, this.connection, this.delayedAcceptPlayer)) {
                this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
            }
        }

        return currentState;
    }

    @Redirect(method = "handleHello", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;READY_TO_ACCEPT:Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;"))
    private ServerLoginPacketListenerImpl.State kilt$useNegotiatingState() {
        return ServerLoginPacketListenerImpl.State.NEGOTIATING;
    }

    @Mixin(targets = "net/minecraft/server/network/ServerLoginPacketListenerImpl$1")
    public static class ThreadInject {
        // TODO: there's meant to be a thread group added here, but i don't know how to
        //       redirect super calls, so hope this doesn't bite me in the ass.

        @Redirect(method = "run", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;READY_TO_ACCEPT:Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;"))
        private ServerLoginPacketListenerImpl.State kilt$useNegotiatingState() {
            return ServerLoginPacketListenerImpl.State.NEGOTIATING;
        }
    }
}