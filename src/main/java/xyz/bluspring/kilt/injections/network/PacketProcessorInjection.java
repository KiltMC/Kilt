package xyz.bluspring.kilt.injections.network;

import org.jetbrains.annotations.ApiStatus;
import xyz.bluspring.kilt.util.KiltHelper;

public interface PacketProcessorInjection {
    @ApiStatus.Internal
    default void scheduleIfPossible(Runnable task) {
        throw KiltHelper.createMixinException(PacketProcessorInjection.class, "scheduleIfPossible");
    }
}
