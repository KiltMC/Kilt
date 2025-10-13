package xyz.bluspring.kilt.injections.world.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.EntityCapability;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface EntityInjection {
    void updateFluidHeightAndDoFluidPushing();
    void updateFluidHeightAndDoFluidPushing(Predicate<FluidState> shouldUpdate);

    @Nullable
    default <T, C extends @Nullable Object> T getCapability(EntityCapability<T, C> capability, C context) {
        return capability.getCapability((Entity) this, context);
    }

    @Nullable
    default <T> T getCapability(EntityCapability<T, @Nullable Void> capability) {
        return capability.getCapability((Entity) this, null);
    }
}
