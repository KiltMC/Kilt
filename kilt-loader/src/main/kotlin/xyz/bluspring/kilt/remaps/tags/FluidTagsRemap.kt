package xyz.bluspring.kilt.remaps.tags

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.material.Fluid

object FluidTagsRemap {
    @JvmStatic
    fun create(name: ResourceLocation): TagKey<Fluid> {
        return TagKey.create(Registry.FLUID_REGISTRY, name)
    }
}