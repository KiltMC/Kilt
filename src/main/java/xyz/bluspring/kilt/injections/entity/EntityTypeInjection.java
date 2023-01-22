package xyz.bluspring.kilt.injections.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;

public interface EntityTypeInjection<T extends Entity> {
    default T customClientSpawn(PlayMessages.SpawnEntity packet, Level world) {
        throw new IllegalStateException();
    }
}
