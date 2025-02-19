package xyz.bluspring.kilt.injections.world.entity;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;

public interface EntityInjection {
    default float getEyeHeightAccess(Pose pose, EntityDimensions dimensions) {
        throw new IllegalStateException();
    }
}
