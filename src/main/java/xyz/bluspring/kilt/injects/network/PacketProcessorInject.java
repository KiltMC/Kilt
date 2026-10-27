package xyz.bluspring.kilt.injects.network;

import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;

import net.neoforged.neoforge.network.handling.QueuedPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.network.PacketProcessorInjection;
import xyz.bluspring.kilt.workarounds.network.PacketProcessorListenerWorkaround;

import net.minecraft.network.PacketProcessor;

@Mixin(PacketProcessor.class)
public abstract class PacketProcessorInject implements PacketProcessorInjection {
    @Shadow private boolean closed;
    @Shadow @Final private Queue<PacketProcessor.ListenerAndPacket<?>> packetsToBeHandled;

    @Override
    public void scheduleIfPossible(Runnable task) {
        if (this.closed) {
            throw new RejectedExecutionException("Server already shutting down");
        } else {
            this.packetsToBeHandled.add(new PacketProcessor.ListenerAndPacket<>(PacketProcessorListenerWorkaround.INSTANCE, new QueuedPacket.CustomPayload(task)));
        }
    }
}
