package xyz.bluspring.kilt.injections.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.connection.ConnectionType;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface RegistryFriendlyByteBufInjection {
    static Function<ByteBuf, RegistryFriendlyByteBuf> decorator(RegistryAccess registryAccess, ConnectionType connectionType) {
        return buf -> {
            var friendlyBuf = new RegistryFriendlyByteBuf(buf, registryAccess);
            friendlyBuf.kilt$setConnectionType(connectionType);

            return friendlyBuf;
        };
    }

    static Function<ByteBuf, RegistryFriendlyByteBuf> kilt$wrappedDecorator(ConnectionType connectionType, Function<ByteBuf, RegistryFriendlyByteBuf> wrapped) {
        return buf -> {
            var original = wrapped.apply(buf);
            original.kilt$setConnectionType(connectionType);
            return original;
        };
    }

    default ConnectionType getConnectionType() {
        throw KiltHelper.createMixinException(RegistryFriendlyByteBufInjection.class, "getConnectionType");
    }

    default void kilt$setConnectionType(ConnectionType connectionType) {
        throw KiltHelper.createMixinException(RegistryFriendlyByteBufInjection.class, "kilt$setConnectionType");
    }
}
