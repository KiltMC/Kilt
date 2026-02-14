package xyz.bluspring.kilt.injections.server.network;

import net.neoforged.neoforge.network.connection.ConnectionType;
import xyz.bluspring.kilt.util.KiltHelper;

public interface CommonListenerCookieInjection {
    default ConnectionType connectionType() {
        throw KiltHelper.createMixinException(CommonListenerCookieInjection.class, "connectionType");
    }

    default void kilt$setConnectionType(ConnectionType connectionType) {
        throw KiltHelper.createMixinException(CommonListenerCookieInjection.class, "kilt$setConnectionType");
    }
}
