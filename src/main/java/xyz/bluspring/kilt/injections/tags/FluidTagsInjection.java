package xyz.bluspring.kilt.injections.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public interface FluidTagsInjection {
    static TagKey<Fluid> create(ResourceLocation name) {
        return TagKey.create(Registry.FLUID_REGISTRY, name);
    }
}
