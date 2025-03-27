package xyz.bluspring.kilt.injections.world.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface BaseSpawnerInjection {
    Entity getSpawnerEntity();
    BlockEntity getSpawnerBlockEntity();
}
