package xyz.bluspring.kilt.injections.world.entity;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.material.FluidState;

import java.util.function.Predicate;

public interface EntityInjection {
    float getEyeHeightAccess(Pose pose, EntityDimensions size);

    void updateFluidHeightAndDoFluidPushing();
    void updateFluidHeightAndDoFluidPushing(Predicate<FluidState> shouldUpdate);
}
