// TRACKED HASH: a63662bfef4ff2ea5679f3fbb6241dba9f545a60
package xyz.bluspring.kilt.injects.server.network;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.network.ServerConnectionListener;

@Mixin(ServerConnectionListener.class)
public abstract class ServerConnectionListenerInject {
    // Kilt TODO: do we really need this?
}
