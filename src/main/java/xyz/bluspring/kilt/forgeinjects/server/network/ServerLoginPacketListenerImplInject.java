package xyz.bluspring.kilt.forgeinjects.server.network;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
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

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplInject {
    @Shadow @Final public Connection connection;

    @Shadow
    ServerLoginPacketListenerImpl.State state;

    @Shadow @Nullable private ServerPlayer delayedAcceptPlayer;

    @Shadow private int tick;

    @Shadow public abstract void disconnect(Component component);

    @WrapWithCondition(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"), method = "handleCustomQueryPacket")
    public boolean kilt$handleForgeCustomQuery(ServerLoginPacketListenerImpl instance, Component component, @Local ServerboundCustomQueryPacket packet) {
        return !NetworkHooks.onCustomPayload(packet, this.connection);
    }

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void kilt$addNegotiationState(CallbackInfo ci) {
        if (this.state == ServerLoginPacketListenerImpl.State.NEGOTIATING) {
            ci.cancel();

            boolean negotiationComplete = NetworkHooks.tickNegotiation((ServerLoginPacketListenerImpl) (Object) this, this.connection, this.delayedAcceptPlayer);

            if (negotiationComplete)
                this.state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;

            if (this.tick++ == 600) {
                this.disconnect(Component.translatable("multiplayer.disconnect.slow_login"));
            }
        }
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;READY_TO_ACCEPT:Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;"), method = "handleHello")
    public ServerLoginPacketListenerImpl.State kilt$setStateToNegotiating() {
        return ServerLoginPacketListenerImpl.State.NEGOTIATING;
    }

    @Mixin(targets = "net/minecraft/server/network/ServerLoginPacketListenerImpl$1")
    public static class ThreadInject {
        @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;READY_TO_ACCEPT:Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;"), method = "run")
        public ServerLoginPacketListenerImpl.State kilt$setStateToNegotiating() {
            return ServerLoginPacketListenerImpl.State.NEGOTIATING;
        }

        // TODO: there's meant to be a thread group added here, but i don't know how to
        //       redirect super calls, so hope this doesn't bite me in the ass.
    }
}
