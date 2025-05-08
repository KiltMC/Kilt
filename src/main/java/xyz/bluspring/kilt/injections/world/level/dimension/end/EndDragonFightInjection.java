package xyz.bluspring.kilt.injections.world.level.dimension.end;

import net.minecraft.server.level.ServerPlayer;

public interface EndDragonFightInjection {
    void addPlayer(ServerPlayer player);
    void removePlayer(ServerPlayer player);
}
