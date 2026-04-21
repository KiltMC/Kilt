package xyz.bluspring.kilt.mixin.workarounds;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.RunningOnDifferentThreadException;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    // Sable no-ops the thread assertion on Fabric, which breaks a lot of mods... need to make sure they still work :/
    @WrapOperation(method = "handleBundlePacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V"))
    private <T extends PacketListener> void kilt$workaround$wrapTryCatchForInvalidThread(Packet<T> instance, T t, Operation<Void> original) {
        try {
            original.call(instance, t);
        } catch (RunningOnDifferentThreadException ignored) {
            Minecraft.getInstance().submit(() -> original.call(instance, t));
        }
    }
}
