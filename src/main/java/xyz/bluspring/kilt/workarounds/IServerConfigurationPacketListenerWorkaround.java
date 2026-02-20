package xyz.bluspring.kilt.workarounds;

import net.minecraft.server.network.ConfigurationTask;

public interface IServerConfigurationPacketListenerWorkaround {
    void finishCurrentTask(ConfigurationTask.Type task);
}
