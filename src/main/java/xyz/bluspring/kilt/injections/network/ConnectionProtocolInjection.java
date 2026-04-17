package xyz.bluspring.kilt.injections.network;

import xyz.bluspring.kilt.util.KiltHelper;

public interface ConnectionProtocolInjection {
    default boolean isPlay() {
        throw KiltHelper.createMixinException(ConnectionProtocolInjection.class, "isPlay");
    }

    default boolean isConfiguration() {
        throw KiltHelper.createMixinException(ConnectionProtocolInjection.class, "isConfiguration");
    }
}
