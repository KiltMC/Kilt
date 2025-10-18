package xyz.bluspring.kilt.injections.server;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import xyz.bluspring.kilt.util.KiltHelper;

public interface MinecraftServerInjection {
    default long[] getTickTime(ResourceKey<Level> dim) {
        throw new IllegalStateException();
    }

    default String getStatusJson() {
        throw new IllegalStateException();
    }

    default MinecraftServer.ReloadableResources getServerResources() {
        throw KiltHelper.createMixinException(MinecraftServerInjection.class, "getServerResources");
    }
}
