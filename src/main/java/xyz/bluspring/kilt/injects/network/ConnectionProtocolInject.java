package xyz.bluspring.kilt.injects.network;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.network.ConnectionProtocolInjection;

import net.minecraft.network.ConnectionProtocol;

@Mixin(ConnectionProtocol.class)
public abstract class ConnectionProtocolInject implements ConnectionProtocolInjection {
    @Shadow @Final public static ConnectionProtocol PLAY;
    @Shadow @Final public static ConnectionProtocol CONFIGURATION;

    @Override
    public boolean isPlay() {
        return (Object) this == PLAY;
    }

    @Override
    public boolean isConfiguration() {
        return (Object) this == CONFIGURATION;
    }
}
