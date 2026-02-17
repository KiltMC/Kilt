package xyz.bluspring.kilt.mixin.workarounds;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.*;
import xyz.bluspring.kilt.workarounds.ICommonPacketListenerWorkaround;

@Implements(@Interface(iface = ICommonPacketListenerWorkaround.class, prefix = "kilt$i$", remap = Interface.Remap.NONE))
@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {
    @Shadow
    public abstract void send(Packet<?> packet);

    @Unique(silent = true)
    public void kilt$i$send(Packet<?> packet) {
        this.send(packet);
    }
}
