package xyz.bluspring.kilt.mixin.workarounds.method_remap_workaround;

import net.neoforged.neoforge.common.extensions.IServerConfigurationPacketListenerExtension;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.workarounds.IServerConfigurationPacketListenerWorkaround;

import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;


@Implements(@Interface(iface = IServerConfigurationPacketListenerWorkaround.class, prefix = "kilt$i$"))
@Mixin(ServerConfigurationPacketListener.class)
public interface ServerConfigurationPacketListenerMixin extends IServerConfigurationPacketListenerExtension {
    @Intrinsic
    default void kilt$i$finishCurrentTask(ConfigurationTask.Type task) {
        this.finishCurrentTask(task);
    }
}
