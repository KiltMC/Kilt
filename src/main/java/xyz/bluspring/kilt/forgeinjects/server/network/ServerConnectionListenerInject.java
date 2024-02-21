package xyz.bluspring.kilt.forgeinjects.server.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerConnectionListener.class)
public class ServerConnectionListenerInject {
    @ModifyReceiver(method = "method_14348", at = @At(value = "INVOKE", target = "Lcom/google/common/util/concurrent/ThreadFactoryBuilder;setDaemon(Z)Lcom/google/common/util/concurrent/ThreadFactoryBuilder;", remap = false))
    private static ThreadFactoryBuilder kilt$addThreadGroupToEventGroup(ThreadFactoryBuilder owner, boolean setDaemon) {
        return owner.setDaemon(setDaemon).setThreadFactory(SidedThreadGroups.SERVER);
    }

    @ModifyReceiver(method = "method_14349", at = @At(value = "INVOKE", target = "Lcom/google/common/util/concurrent/ThreadFactoryBuilder;setDaemon(Z)Lcom/google/common/util/concurrent/ThreadFactoryBuilder;", remap = false))
    private static ThreadFactoryBuilder kilt$addThreadGroupToEpollEventGroup(ThreadFactoryBuilder owner, boolean setDaemon) {
        return owner.setDaemon(setDaemon).setThreadFactory(SidedThreadGroups.SERVER);
    }
}
