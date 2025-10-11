package xyz.bluspring.kilt.injections.world.entity;

import net.minecraft.world.level.material.FluidState;

import java.util.function.Predicate;

public interface EntityInjection {
    void updateFluidHeightAndDoFluidPushing();
    void updateFluidHeightAndDoFluidPushing(Predicate<FluidState> shouldUpdate);
}
