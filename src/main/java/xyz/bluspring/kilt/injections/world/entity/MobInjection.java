package xyz.bluspring.kilt.injections.world.entity;

import net.minecraft.world.entity.EntitySpawnReason;

public interface MobInjection {
    boolean isSpawnCancelled();
    void setSpawnCancelled(boolean cancel);

    EntitySpawnReason getSpawnType();
}
