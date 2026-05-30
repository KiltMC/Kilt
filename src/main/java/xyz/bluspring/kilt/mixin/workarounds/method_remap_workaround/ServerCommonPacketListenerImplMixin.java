package xyz.bluspring.kilt.mixin.workarounds.method_remap_workaround;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.workarounds.ICommonPacketListenerWorkaround;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

@Implements(@Interface(iface = ICommonPacketListenerWorkaround.class, prefix = "kilt$i$", remap = Interface.Remap.NONE))
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
    @Shadow
    public abstract void send(Packet<?> packet);

    @Intrinsic
    public void kilt$i$send(Packet<?> packet) {
        this.send(packet);
    }
}
