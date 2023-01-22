package xyz.bluspring.kilt.injections.network;

import net.minecraftforge.network.NetworkConstants;

public interface ClientIntentionPacketInjection {
    default String getFMLVersion() {
        return NetworkConstants.NETVERSION;
    }
}
