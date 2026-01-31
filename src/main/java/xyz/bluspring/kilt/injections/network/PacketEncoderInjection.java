package xyz.bluspring.kilt.injections.network;

import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import xyz.bluspring.kilt.util.KiltHelper;

public interface PacketEncoderInjection<T extends PacketListener> {
    default ProtocolInfo<T> getProtocolInfo() {
        throw KiltHelper.createMixinException(PacketEncoderInjection.class, "getProtocolInfo");
    }
}
