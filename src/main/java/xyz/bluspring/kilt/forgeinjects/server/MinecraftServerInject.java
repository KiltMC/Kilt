package xyz.bluspring.kilt.forgeinjects.server;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.server.MinecraftServerInjection;

import java.util.Map;

@Mixin(MinecraftServer.class)
public class MinecraftServerInject implements MinecraftServerInjection {
    private Map<ResourceKey<Level>, long[]> perWorldTickTimes = Maps.newIdentityHashMap();

    @Override
    public long[] getTickTime(ResourceKey<Level> dim) {
        return perWorldTickTimes.get(dim);
    }
}
