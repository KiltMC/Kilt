package xyz.bluspring.kilt.injects.server.network;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.extensions.IServerConfigurationPacketListenerExtension;
import net.neoforged.neoforge.network.ConfigurationInitialization;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.payload.ModdedNetworkQueryPayload;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.network.RegistryFriendlyByteBufInjection;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

@Implements({
    @Interface(iface = ServerConfigurationPacketListener.class, prefix = "kilt$i$"),
    @Interface(iface = IServerConfigurationPacketListenerExtension.class, prefix = "kilt$j$")
})
@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplInject extends ServerCommonPacketListenerImplInject implements ServerConfigurationPacketListener {
    @Shadow @Final private Queue<ConfigurationTask> configurationTasks;
    @Shadow public abstract void finishCurrentTask(ConfigurationTask.Type taskType);

    @Inject(method = "startConfiguration", at = @At("HEAD"))
    private void kilt$setupNeoConfiguration(CallbackInfo ci) {
        // Kilt: Injections made into Fabric API's ServerConfigurationNetworkAddon
//        this.send(new RegistrationPayload(RegistrationPayload.UNREGISTER, new ArrayList<>(NetworkRegistry.getInitialServerUnregisterChannels()))); // Kilt: use Fabric API
//        this.send(new RegistrationPayload(RegistrationPayload.REGISTER, new ArrayList<>(NetworkRegistry.getInitialListeningChannels(this.flow())))); // Kilt: use Fabric API
//        this.send(new ModdedNetworkQueryPayload(Map.of()));
//        this.send(new ClientboundPingPacket(0));
    }

    @Inject(method = "startConfiguration", at = @At(value = "NEW", target = "(Ljava/util/List;Lnet/minecraft/core/LayeredRegistryAccess;)Lnet/minecraft/server/network/config/SynchronizeRegistriesTask;"))
    private void kilt$syncNeoRegistries(CallbackInfo ci) {
        // Kilt: Fabric API should handle this
//        ConfigurationInitialization.configureEarlyTasks(this, this.configurationTasks::add);
    }

    @Inject(method = "addOptionalTasks", at = @At("TAIL"))
    private void kilt$gatherConfigurationTasks(CallbackInfo ci) {
        this.configurationTasks.addAll(ModLoader.postEventWithReturn(new RegisterConfigurationTasksEvent(this)).getConfigurationTasks());
    }

    @Intrinsic(displace = true)
    public void kilt$i$handleCustomPayload(ServerboundCustomPayloadPacket packet) {
        if (packet.payload() instanceof ModdedNetworkQueryPayload payload) {
            this.connectionType = ConnectionType.NEOFORGE;
            NetworkRegistry.initializeNeoForgeConnection(this, payload.queries());
            return;
        }

        super.handleCustomPayload(packet);
    }

    @Intrinsic(displace = true)
    public void kilt$i$handlePong(ServerboundPongPacket packet) {
        super.handlePong(packet);

        if (packet.getId() == 0) {
            if (!this.connectionType.isNeoForge() && !NetworkRegistry.initializeOtherConnection(this)) {
                return;
            }

            // this.runConfiguration();
            // Kilt TODO: do we handle pong behaviour?
        }
    }

    @ModifyExpressionValue(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryFriendlyByteBuf;decorator(Lnet/minecraft/core/RegistryAccess;)Ljava/util/function/Function;"))
    private Function<ByteBuf, RegistryFriendlyByteBuf> kilt$appendConnectionTypeToDecorator(Function<ByteBuf, RegistryFriendlyByteBuf> original) {
        return RegistryFriendlyByteBufInjection.kilt$wrappedDecorator(this.connectionType, original);
    }

    @Inject(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setupOutboundProtocol(Lnet/minecraft/network/ProtocolInfo;)V", shift = At.Shift.AFTER))
    private void kilt$handleConfigurationFinish(ServerboundFinishConfigurationPacket packet, CallbackInfo ci) {
        if (this.connectionType == ConnectionType.OTHER) {
            NetworkRegistry.initializeNeoForgeConnection(this, Map.of());
        }

        NetworkRegistry.onConfigurationFinished(this);
    }

    @ModifyExpressionValue(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerConfigurationPacketListenerImpl;createCookie(Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/network/CommonListenerCookie;"))
    private CommonListenerCookie kilt$attachConnectionTypeToCookie(CommonListenerCookie original) {
        original.kilt$setConnectionType(this.connectionType);
        return original;
    }

    @Intrinsic
    public void kilt$j$finishCurrentTask(ConfigurationTask.Type task) {
        this.finishCurrentTask(task);
    }
}
