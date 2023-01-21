package xyz.bluspring.kilt.injections.server;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface MinecraftServerInjection {
    default long[] getTickTime(ResourceKey<Level> dim) {
        throw new IllegalStateException();
    }


}
