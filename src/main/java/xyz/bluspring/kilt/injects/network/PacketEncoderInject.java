package xyz.bluspring.kilt.injects.network;

import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.network.PacketEncoderInjection;

@Mixin(PacketEncoder.class)
public abstract class PacketEncoderInject<T extends PacketListener> implements PacketEncoderInjection<T> {
    @Shadow @Final private ProtocolInfo<T> protocolInfo;

    @Override
    public ProtocolInfo<T> getProtocolInfo() {
        return this.protocolInfo;
    }
}
