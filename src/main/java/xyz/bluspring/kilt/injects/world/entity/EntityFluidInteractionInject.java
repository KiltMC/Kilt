package xyz.bluspring.kilt.injects.world.entity;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.InFluidPredicate;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.entity.EntityFluidInteractionInjection;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.material.Fluid;

@Mixin(EntityFluidInteraction.class)
public abstract class EntityFluidInteractionInject implements EntityFluidInteractionInjection {
    @Shadow @Final private Map<TagKey<Fluid>, EntityFluidInteraction.Tracker> trackerByFluid;
    @Shadow public abstract double getFluidHeight(TagKey<Fluid> fluid);
    @Shadow public abstract boolean isInFluid(TagKey<Fluid> fluid);
    @Shadow public abstract boolean isEyeInFluid(TagKey<Fluid> fluid);

    @Unique private final Map<FluidType, EntityFluidInteraction.Tracker> kilt$trackerByFluid = new Reference2ObjectArrayMap<>();

    @Inject(method = "getTrackerFor", at = @At("HEAD"), cancellable = true)
    private void kilt$tryGetTrackerForFluidType(Fluid fluid, CallbackInfoReturnable<EntityFluidInteraction.Tracker> cir) {
        try {
            var fluidType = fluid.getFluidType();
            if (fluidType != null) {
                cir.setReturnValue(this.getTrackerFor(fluidType));
            }
        } catch (Throwable ignored) {}
    }

    @Unique
    private EntityFluidInteraction.Tracker getTrackerFor(FluidType fluid) {
        var existingFluidTag = kilt$tryFindVanillaFluid(fluid);
        if (existingFluidTag != null) {
            return this.trackerByFluid.computeIfAbsent(existingFluidTag, _ -> new EntityFluidInteraction.Tracker());
        }

        return this.kilt$trackerByFluid.computeIfAbsent(fluid, _ -> new EntityFluidInteraction.Tracker());
    }

    @Override
    public void applyCurrentTo(FluidType fluid, Entity entity, double scale) {
        var tracker = this.kilt$trackerByFluid.get(fluid);
        // Kilt: Vanilla/Fabric handling
        if (tracker == null) {
            var fluidTag = kilt$tryFindVanillaFluid(fluid);

            if (fluidTag != null)
                tracker = this.trackerByFluid.get(fluidTag);
        }

        if (tracker != null) {
            tracker.applyCurrentTo(entity, scale);
        }
    }

    @Override
    public void applyCurrentTo(Entity entity) {
        // Kilt: apply to Vanilla
        for (Map.Entry<TagKey<Fluid>, EntityFluidInteraction.Tracker> entry : this.trackerByFluid.entrySet()) {
            var type = getFluidTypeByTag(entry.getKey());
            if (type == null)
                continue;

            if (entry.getValue().height > 0) {
                entry.getValue().applyCurrentTo(entity, entity.getFluidMotionScale(type));
            }
        }

        for (Map.Entry<FluidType, EntityFluidInteraction.Tracker> entry : this.kilt$trackerByFluid.entrySet()) {
            if (entry.getValue().height > 0) {
                entry.getValue().applyCurrentTo(entity, entity.getFluidMotionScale(entry.getKey()));
            }
        }
    }

    @Override
    public double getFluidHeight(FluidType fluid) {
        var fluidTag = kilt$tryFindVanillaFluid(fluid);
        if (fluidTag != null)
            return this.getFluidHeight(fluidTag);

        var tracker = this.kilt$trackerByFluid.get(fluid);
        return tracker != null ? tracker.height : 0.0;
    }

    @Override
    public <E extends Entity> double getMaxFluidHeightMatching(E entity, InFluidPredicate<E> predicate) {
        double maxHeight = 0.0;
        for (Map.Entry<FluidType, EntityFluidInteraction.Tracker> entry : this.kilt$trackerByFluid.entrySet()) {
            double height = entry.getValue().height;
            if (height > maxHeight && predicate.test(entity, entry.getKey(), height)) {
                maxHeight = height;
            }
        }

        return maxHeight;
    }

    @Override
    public FluidType getMaxHeightFluidType() {
        double maxHeight = 0.0;
        var maxHeightType = NeoForgeMod.EMPTY_TYPE.value();

        // Kilt: handle Vanilla
        for (Map.Entry<TagKey<Fluid>, EntityFluidInteraction.Tracker> entry : this.trackerByFluid.entrySet()) {
            var fluidType = getFluidTypeByTag(entry.getKey());
            if (fluidType == null)
                continue;

            double height = entry.getValue().height;
            if (height > maxHeight) {
                maxHeight = height;
                maxHeightType = fluidType;
            }
        }

        for (Map.Entry<FluidType, EntityFluidInteraction.Tracker> entry : this.kilt$trackerByFluid.entrySet()) {
            double height = entry.getValue().height;
            if (height > maxHeight) {
                maxHeight = height;
                maxHeightType = entry.getKey();
            }
        }

        return maxHeightType;
    }

    @Override
    public boolean isInFluid(FluidType fluid) {
        var fluidTag = kilt$tryFindVanillaFluid(fluid);
        if (fluidTag != null)
            return this.isInFluid(fluidTag);

        return this.getFluidHeight(fluid) > 0;
    }

    @Override
    public boolean isInAnyFluid() {
        for (EntityFluidInteraction.Tracker tracker : this.trackerByFluid.values()) {
            if (tracker.height > 0)
                return true;
        }

        for (EntityFluidInteraction.Tracker tracker : this.kilt$trackerByFluid.values()) {
            if (tracker.height > 0)
                return true;
        }

        return false;
    }

    @Override
    public <E extends Entity> boolean isInFluidMatching(E entity, InFluidPredicate<E> predicate) {
        for (Map.Entry<TagKey<Fluid>, EntityFluidInteraction.Tracker> entry : this.trackerByFluid.entrySet()) {
            var fluidType = getFluidTypeByTag(entry.getKey());
            if (fluidType == null)
                continue;

            double height = entry.getValue().height;
            if (height > 0 && predicate.test(entity, fluidType, height))
                return true;
        }

        for (Map.Entry<FluidType, EntityFluidInteraction.Tracker> entry : this.kilt$trackerByFluid.entrySet()) {
            double height = entry.getValue().height;
            if (height > 0 && predicate.test(entity, entry.getKey(), height))
                return true;
        }

        return false;
    }

    @Override
    public boolean isEyeInFluid(FluidType fluid) {
        var fluidTag = kilt$tryFindVanillaFluid(fluid);
        if (fluidTag != null)
            return this.isEyeInFluid(fluidTag);

        var tracker = this.kilt$trackerByFluid.get(fluid);
        return tracker != null && tracker.eyesInside;
    }

    @Override
    public <E extends Entity> boolean isEyeInFluidMatching(E entity, InFluidPredicate<E> predicate) {
        for (Map.Entry<TagKey<Fluid>, EntityFluidInteraction.Tracker> entry : this.trackerByFluid.entrySet()) {
            var fluidType = getFluidTypeByTag(entry.getKey());
            if (fluidType == null)
                continue;

            var tracker = entry.getValue();
            if (tracker.eyesInside && predicate.test(entity, fluidType, tracker.height))
                return true;
        }

        for (Map.Entry<FluidType, EntityFluidInteraction.Tracker> entry : this.kilt$trackerByFluid.entrySet()) {
            var tracker = entry.getValue();
            if (tracker.eyesInside && predicate.test(entity, entry.getKey(), tracker.height))
                return true;
        }

        return false;
    }

    @Override
    public FluidType getFirstEyeInFluid() {
        for (Map.Entry<TagKey<Fluid>, EntityFluidInteraction.Tracker> entry : this.trackerByFluid.entrySet()) {
            var fluidType = getFluidTypeByTag(entry.getKey());
            if (fluidType == null)
                continue;

            if (entry.getValue().eyesInside) {
                return fluidType;
            }
        }

        for (Map.Entry<FluidType, EntityFluidInteraction.Tracker> entry : this.kilt$trackerByFluid.entrySet()) {
            if (entry.getValue().eyesInside)
                return entry.getKey();
        }

        return NeoForgeMod.EMPTY_TYPE.value();
    }

    @CreateStatic
    private static FluidType getFluidTypeByTag(TagKey<Fluid> fluidTag) {
        return EntityFluidInteractionInjection.getFluidTypeByTag(fluidTag);
    }

    @Unique
    private static @Nullable TagKey<Fluid> kilt$tryFindVanillaFluid(FluidType type) {
        if (type == NeoForgeMod.WATER_TYPE.value())
            return FluidTags.WATER;
        else if (type == NeoForgeMod.LAVA_TYPE.value())
            return FluidTags.LAVA;
        else
            return null;
    }

    @Mixin(targets = "net.minecraft.world.entity.EntityFluidInteraction.Tracker")
    public abstract static class TrackerInject implements TrackerInjection {
        @Shadow public abstract void reset();

        @Unique private int idleTicks = 0;

        @Override
        public boolean resetAndCheckUnused() {
            this.reset();
            this.idleTicks++;
            return this.idleTicks > 20;
        }
    }
}
