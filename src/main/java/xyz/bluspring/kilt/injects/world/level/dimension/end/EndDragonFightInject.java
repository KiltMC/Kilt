package xyz.bluspring.kilt.injects.world.level.dimension.end;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.level.dimension.end.EndDragonFightInjection;

@Mixin(EndDragonFight.class)
public abstract class EndDragonFightInject implements EndDragonFightInjection {
    @Shadow @Final private ServerBossEvent dragonEvent;

    @Override
    public void addPlayer(ServerPlayer player) {
        this.dragonEvent.addPlayer(player);
    }

    @Override
    public void removePlayer(ServerPlayer player) {
        this.dragonEvent.removePlayer(player);
    }
}
