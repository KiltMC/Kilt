package xyz.bluspring.kilt.injections.world.entity;

import net.minecraft.world.entity.MobSpawnType;

public interface MobInjection {
    boolean isSpawnCancelled();
    void setSpawnCancelled(boolean cancel);

    MobSpawnType getSpawnType();
}
