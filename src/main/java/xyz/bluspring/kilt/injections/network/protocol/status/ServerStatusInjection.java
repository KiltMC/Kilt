package xyz.bluspring.kilt.injections.network.protocol.status;

import xyz.bluspring.kilt.util.KiltHelper;

public interface ServerStatusInjection {
    default boolean isModded() {
        throw KiltHelper.createMixinException(ServerStatusInjection.class, "isModded");
    }

    default void kilt$setModded(boolean isModded) {
        throw KiltHelper.createMixinException(ServerStatusInjection.class, "kilt$setModded");
    }
}
