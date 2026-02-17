package xyz.bluspring.kilt.mixin.workarounds;

import net.minecraft.network.protocol.Packet;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.workarounds.ICommonPacketListenerWorkaround;

@Mixin(ICommonPacketListener.class)
public interface ICommonPacketListenerMixin extends ICommonPacketListenerWorkaround {
    @Override
    void send(Packet<?> packet);
}
