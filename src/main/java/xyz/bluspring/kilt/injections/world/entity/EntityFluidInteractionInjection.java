package xyz.bluspring.kilt.injections.world.entity;

import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.InFluidPredicate;
import xyz.bluspring.kilt.util.KiltHelper;
import xyz.bluspring.kilt.workarounds.FluidBehaviorAsFluidType;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.registry.fluid.EntityFluidInteractionRegistry;

public interface EntityFluidInteractionInjection {
    default void applyCurrentTo(FluidType fluid, Entity entity, double scale) {
        throw KiltHelper.createMixinException(EntityFluidInteractionInjection.class, "applyCurrentTo");
    }

    default void applyCurrentTo(Entity entity) {
        throw KiltHelper.createMixinException(EntityFluidInteractionInjection.class, "applyCurrentTo");
    }

    default double getFluidHeight(FluidType fluid) {
        throw KiltHelper.createMixinException(EntityFluidInteractionInjection.class, "getFluidHeight");
    }

    default <E extends Entity> double getMaxFluidHeightMatching(E entity, InFluidPredicate<E> predicate) {
        throw KiltHelper.createMixinException(EntityFluidInteractionInjection.class, "getMaxFluidHeightMatching");
    }

    default FluidType getMaxHeightFluidType() {
        throw KiltHelper.createMixinException(EntityFluidInteractionInjection.class, "getMaxHeightFluidType");
    }

    default boolean isInFluid(FluidType fluid) {
        throw KiltHelper.createMixinException(EntityFluidInteractionInjection.class, "isInFluid");
    }

    default boolean isInAnyFluid() {
        throw KiltHelper.createMixinException(EntityFluidInteractionInjection.class, "isInAnyFluid");
    }

    default <E extends Entity> boolean isInFluidMatching(E entity, InFluidPredicate<E> predicate) {
        throw KiltHelper.createMixinException(EntityFluidInteractionInjection.class, "isInFluidMatching");
    }

    default boolean isEyeInFluid(FluidType fluid) {
        throw KiltHelper.createMixinException(EntityFluidInteractionInjection.class, "isEyeInFluid");
    }

    default <E extends Entity> boolean isEyeInFluidMatching(E entity, InFluidPredicate<E> predicate) {
        throw KiltHelper.createMixinException(EntityFluidInteractionInjection.class, "isEyeInFluidMatching");
    }

    default FluidType getFirstEyeInFluid() {
        throw KiltHelper.createMixinException(EntityFluidInteractionInjection.class, "getFirstEyeInFluid");
    }

    static FluidType getFluidTypeByTag(TagKey<Fluid> fluidTag) {
        if (fluidTag == FluidTags.WATER)
            return NeoForgeMod.WATER_TYPE.value();
        else if (fluidTag == FluidTags.LAVA)
            return NeoForgeMod.LAVA_TYPE.value();
        else {
            // Kilt: Try to figure out a fluid type
            var behaviour = EntityFluidInteractionRegistry.getFluidBehavior(fluidTag);
            if (behaviour != null) {
                return FluidBehaviorAsFluidType.computeIfAbsent(behaviour, fluidTag);
            }

            throw new IllegalArgumentException("Cannot look up tracker by tag for non-vanilla fluid: " + fluidTag);
        }
    }

    interface TrackerInjection {
        default boolean resetAndCheckUnused() {
            throw KiltHelper.createMixinException(TrackerInjection.class, "resetAndCheckUnused");
        }
    }
}
