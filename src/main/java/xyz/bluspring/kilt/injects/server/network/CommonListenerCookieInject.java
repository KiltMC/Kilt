package xyz.bluspring.kilt.injects.server.network;

import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.server.network.CommonListenerCookieInjection;

@Mixin(CommonListenerCookie.class)
public abstract class CommonListenerCookieInject implements CommonListenerCookieInjection {
    private ConnectionType connectionType = ConnectionType.OTHER;

    @Override
    public ConnectionType connectionType() {
        return this.connectionType;
    }

    @Override
    public void kilt$setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }
}
