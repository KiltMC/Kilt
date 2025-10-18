package xyz.bluspring.kilt.injects.network.protocol;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.neoforged.neoforge.network.bundle.BundlePacketUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BundlePacket.class)
public abstract class BundlePacketInject<T extends PacketListener> {
    @Shadow @Final @Mutable private Iterable<Packet<? super T>> packets;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$flattenBundlePackets(Iterable<Packet<? super T>> packets, CallbackInfo ci) {
        this.packets = BundlePacketUtils.flatten(this.packets);
    }
}
