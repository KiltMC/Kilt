package xyz.bluspring.kilt.injections.world.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.EntityCapability;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.Predicate;

public interface EntityInjection {
    default <T, C extends @Nullable Object> T getCapability(EntityCapability<T, C> capability, C context) {
        throw KiltHelper.createMixinException(EntityInjection.class, "getCapability");
    }

    default <T> T getCapability(EntityCapability<T, @Nullable Void> capability) {
        throw KiltHelper.createMixinException(EntityInjection.class, "getCapability");
    }
}
