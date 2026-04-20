package xyz.bluspring.kilt.mixin.debug;

import com.moulberry.mixinconstraints.annotations.IfDevEnvironment;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;

@IfDevEnvironment
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    /*@WrapOperation(method = "handleBundlePacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V"))
    private <T extends PacketListener> void kilt$debug$actuallyComplainIfAnErrorOccurs(Packet<T> instance, T t, Operation<Void> original) {
        try {
            original.call(instance, t);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }*/
}
